package ru.mail.airenea;

import java.net.MalformedURLException;
import java.net.URL;

// Класс, содержащий пару значений [URL, глубина].
public class URLDepthPair {


    // Строка, содержащая URL страницы с которой начинается поиск
    private String url;

    // Положительное целое число – максимальная глубина поиска
    private int depth;

    //Конструктор, принимает пару значений [URL, глубина]
    public 	URLDepthPair(String newUrl, int newDepth) {
        url = new String(newUrl);
        depth = newDepth;
    }

    // Метод, который печатает пару значений [URL, глубина]на экране
    public String toString() {
        String depthStr = Integer.toString(depth);
        return "[ " + url + ", " + depthStr + " ]";
    }

    // Метод, возвращающий URL
    public String getURL() {
        return this.url;
    }

    // Метод, возвращающий глубину
    public int getDepth() {
        return this.depth;
    }

    // Метод, возвращающий имя хоста из URL. Может вызвать и обработать MalformedURLException
    public String getHost() {
        try {
            URL tempURL = new URL(url);

            return tempURL.getHost();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException in hostname: " + e.getMessage() + "\n in link:" + url);
            return null;
        }
    }

    // Метод, возвращающий путь к веб странице на сервере. Может вызвать и обработать MalformedURLException
    public String getPath() {
        try {
            URL tempURL = new URL(url);
            return tempURL.getPath();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException in path: " + e.getMessage() + "\n in link:" + url);
            return null;
        }
    }
}
