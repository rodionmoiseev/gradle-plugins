package com.github.rodionmoiseev.gradle.plugins;

/**
 * SpellingExtension
 *
 * @author rodion
 * @version 0.2
 * @since 2012/11/08
 */
public class SpellingExtension {
    /**
     * Named used for the "Accepted Words" dictionary.
     * Must match 'user.name' property in IntelliJ, otherwise
     * dictionary will not be recognised.
     */
    String userName = System.getProperty("user.name")

    /**
     * Accepts a list of string, a File, or a list of Files.
     * When files are specified, their contents are loaded and
     * exploded into a flat list, with duplicates removed.
     */
    Object acceptedWords = []

    /**
     * A list of directories to scan for dictionary files.
     */
    List<File> dictionaries = []

    /**
     * Charset to use when loading accepted words from files
     */
    String charset = "UTF-8"

    public List<String> loadAcceptedWords() {
        Set<String> orderedWordSet = new LinkedHashSet<String>()
        if (acceptedWords instanceof List) {
            for (word in acceptedWords) {
                loadAcceptedWords(word, orderedWordSet)
            }
        } else {
            loadAcceptedWords(acceptedWords, orderedWordSet)
        }
        return new ArrayList<String>(orderedWordSet)
    }

    private void loadAcceptedWords(Object obj, Set<String> res) {
        if (obj instanceof File) {
            loadAcceptedWordsFromFile((File) obj, res)
        } else {
            res.add(String.valueOf(obj))
        }
    }

    private void loadAcceptedWordsFromFile(File file, res) {
        try {
            file.readLines(charset).each { word -> res.add(word) }
        } catch (IOException e) {
            throw new IdeaUtilsPluginException("Failed to load accepted words dictioary file: ${file}", e)
        }
    }
}
