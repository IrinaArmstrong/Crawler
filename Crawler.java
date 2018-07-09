package ru.mail.airenea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**/
/*
 * Класс,который реализует основной функционал приложения.
 * Принимает в качетве аргументов два значения типа String: URL и глубину поиска,
 * которую в последствии преобразовывает в переменную типа int.
 * Сохраняет оба значения как объект класса URLDepthPair.
 * */
//
public class Crawler {

    // Констанда, содержащая префикс URL,
    // нужный для того чтобы определить имеет ли URL правильный формат
    public static final String URL_PREFIX = "a href=\"";

    // Константа, содержащая постфикс URL
    public static final String URL_POSTEFIX = "\"";

    public static void main(String[] args) {

        // Переменная, для хранения текущей глубины поиска
        int firstDepth = 0;
        // Переменная, для хранения изначальной ссылки, введенной пользователем
        String firstUrl = "";

        //Ввод данных с клавиатуры
        try {
            Scanner in = new Scanner(System.in);
            System.out.println("Введите ссылку: ");
            firstUrl = in.nextLine();
            System.out.println("Введите глубину поиска (целое положительное число): ");
            firstDepth = in.nextInt();
        }
        catch (Exception ex) {
            System.out.println("Не верный формат ввода данных!");
            System.exit(1);
        }
        if(firstDepth < 0) {
            // Если глубина не положительное число, то выходим из программы
            System.out.println("Не верный формат ввода данных!");
            System.exit(1);
        }
        if(firstDepth < 0) {
            // Если глубина не положительное число, то выходим из программы
            System.out.println("Не верный формат ввода данных! Глубина поиска не может быть отрицательным числом.");
            System.exit(1);
        }



        // Содержит все известные на данный момент ссылки
        LinkedList<URLDepthPair> allURLs = new LinkedList<URLDepthPair>();

        // Включает еще не просмотренные ссылки, когда список оказывается пуст, работа закончена
        LinkedList<URLDepthPair> unusedURLs = new LinkedList<URLDepthPair>();

        // Переменная, в которой хранится пара URL-глубина, которую ввел пользователь
        URLDepthPair gotURLDepthPair = new URLDepthPair(firstUrl, 0);

        unusedURLs.add(gotURLDepthPair);

        // Список просмотренных URL
        ArrayList <String> usedURLs = new ArrayList <String>();
        usedURLs.add(gotURLDepthPair.getURL());

        // Обрабатываем URL, пока спиок не просмотренных пар URL-глубина не оказывается пуст
        while(unusedURLs.isEmpty() != true) {

            // Берем из списка не просмотренных пар URL-глубина первое значение и удаляем его оттуда
            URLDepthPair currentURLDepthPair = unusedURLs.pop();
            allURLs.add(currentURLDepthPair);

            // Переменная, в которую помещаем текущее значение глубины поиска
            int currentDepth = currentURLDepthPair.getDepth();

            // Список, в который записываем все ссылки, найденные на странице
            LinkedList<String> linksList = new LinkedList<String>();
            linksList = Crawler.getSites(currentURLDepthPair);

            // If we haven't reached the maximum depth, add links from the site
            // that haven't been seen before to pendingURLs and seenURLs.
            if (currentDepth < firstDepth) {

                // Просматриваем список URL, полученных по текущей ссылке
                for (int i = 0; i < linksList.size(); i++) {
                    String newURL = linksList.get(i);

                    // Если ссылка уже просматривалась ранее, то продолжаем просматривать список и ничего не делаем
                    if (usedURLs.contains(newURL)) {
                        continue;
                    }

                    // Если ссылка еще не просмотренная, то создаем для нее пару URL-глубина,
                    // Добавляем пару в список непросмотренных, а саму ссылку в список использованных ссылок
                    else {
                        URLDepthPair newDepthPair = new URLDepthPair(newURL, currentDepth + 1);
                        unusedURLs.add(newDepthPair);
                        usedURLs.add(newURL);
                    }
                }
            }
        }

        // Выводим список всех посещенных ссылок
        System.out.println("Найдено всего - " + allURLs.size() + " ссылок.");
        Iterator<URLDepthPair> iterator = allURLs.iterator();

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

    }






    // Метод, который возвращает весь список пар URL-глубина которые были просмотрены роботом
    static public LinkedList<String> getSites (URLDepthPair URLDepth) {

        // Список пар URL-глубина, в который записываем все пары просмотренные после перехода по ссылке, которую получили на входе
        LinkedList<String> URLs = new LinkedList<String>();

        String link = URLDepth.getHost();
        String path = URLDepth.getPath();

        // Создаем переменную под сокет-соединение
        Socket socket;

        try {
            // Инициализируем сокет, пробуем соединиться с сервером по полученной ссылке и порту 80
            socket = new Socket(link, 80);
            socket.setSoTimeout(3000);
        }
        // Ошибка при открытии соединения
        catch(Exception e) {
            System.err.println(e.getMessage());
            return URLs;
        }

        // Открываем поток для запроса к серверу, обрабатываем возможную ошибку IOException.
        OutputStream outputStr;
        try {
            outputStr = socket.getOutputStream();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return URLs;
        }

        // Создаем экземпляр класса PrintWriter и
        // вызываем его метод println для передачи строки текста на другую сторону соединения сокета.
        // Параметр autoFlush = true. Тогда данные не будут попадать в буфер передатчика,
        // а будут передаваться сразу после каждого вызова println.
        PrintWriter requestWriter = new PrintWriter(outputStr, true);

        // Отправляем запрос на сервер
        requestWriter.println("GET " + path + " HTTP/1.1");
        requestWriter.println("Host: " + link);
        requestWriter.println("Connection: close");
        requestWriter.println();

        // Открываем входной поток для получения страницы с сервера, обрабатываем возможную ошибку IOException.
        InputStream inputStr;

        try {
            inputStr = socket.getInputStream();
        }

        catch (IOException e){
            System.err.println(e.getMessage());
            return URLs;
        }

        // Создаем экземпляр класса InputStreamReader для преобразования байтов из входного потока в символы
        InputStreamReader inputStrReader = new InputStreamReader(inputStr);

        // Создаем экземпляр класса BufferedReader для считывания символов из потока в буфер
        BufferedReader bufferReader = new BufferedReader(inputStrReader);

        String readLine;
        //boolean end = false;

        // Считываем строку из экземпляра BufferedReader
        while (true) {
            try {
                readLine = bufferReader.readLine();
            }

            catch (IOException e) {
                System.err.println(e.getMessage());
                return URLs;
            }

            // Строка пуста - конец страницы
            if (readLine == null) {
                break;
            }

            // Переменные для хранения индексов при поиске в строке ссылок
            int beginIndex = 0;
            int endIndex = 0;
            int currentIndex = 0;
            Pattern findUrlPattern = Pattern.compile("a href=\".+\"");
            Matcher findUrlMatcher = findUrlPattern.matcher(readLine);

            /*while (findUrlMatcher.find(currentIndex)) {
                // Ищем в полученной строке ссылку, начиная с текущего индекса
                beginIndex = findUrlMatcher.start();
                endIndex = findUrlMatcher.end();
                String foundLink = readLine.substring(beginIndex + URL_PREFIX.length(), endIndex - 1);
                //System.out.println("FoundLink: " + foundLink);
                URLs.add(foundLink);
                currentIndex = endIndex;
            }*/


            while(true) {
                // Ищем в полученной строке ссылку, начиная с текущего индекса
                currentIndex = readLine.indexOf(URL_PREFIX, currentIndex);
                // Не найденно больше ни одного вхождения, значит берем следующую строку
                if (currentIndex == -1) {
                    break;
                }

                // Обходим префикс ссылки и присваиваем начальному индексу ссылки значение текущего индекса - ссылка найдена!
                currentIndex += URL_PREFIX.length();
                beginIndex = currentIndex;

                // Обходим ссылку и присваиваем конечному индексу ссылки значение, найденное при поиске постфикса ссылки
                // Присваиваем текущему индексу значение конечного индекса ссылки
                endIndex = readLine.indexOf(URL_POSTEFIX, currentIndex);
                currentIndex = endIndex;

                // Считываем все символы между начальным и конечным индексами в новую строку - т.е. сохраняем ссылку
                String foundLink = readLine.substring(beginIndex, endIndex);
                URLs.add(foundLink);
            }

        }
        // Возвращаем список URL
        return URLs;

    }
}

