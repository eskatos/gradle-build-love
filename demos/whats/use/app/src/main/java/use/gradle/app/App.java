/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package use.gradle.app;

import use.gradle.list.LinkedList;

import static use.gradle.utilities.StringUtils.join;
import static use.gradle.utilities.StringUtils.split;
import static use.gradle.app.MessageUtils.getMessage;

import org.apache.commons.text.WordUtils;

public class App {
    public static void main(String[] args) {
        LinkedList tokens;
        tokens = split(getMessage());
        String result = join(tokens);
        System.out.println(WordUtils.capitalize(result));
    }
}
