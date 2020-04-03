import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class Utils {

    public static String resolvePath(String path) {
        return resolvePath(path, "./");
    }

    public static String resolvePath(String path, String file) {
        if (Paths.get(path).isAbsolute()) {
            return path;
        }
        return Paths.get("./" + file).getParent().resolve(path).normalize().toString();
    }


    public static Optional<FileContent> readFile(String path) {
        if (!new File(path).isFile()) {
            return Optional.empty();
        }
        FileContent content = new FileContent();
        try {
            Files.lines(Paths.get(path)).forEachOrdered(
                    line -> {
                        if (line.length() > 0) {
                            if (line.startsWith("#include ")) {
                                content.links.add(line);
                            } else {
                                content.simpleRows.add(line);
                            }
                        }
                    }
            );
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(content);
    }

    public static class FileContent {
        private final ArrayList<String> simpleRows = new ArrayList<>();
        private final ArrayList<String> links = new ArrayList<>();

        public ArrayList<String> getSimpleRows() {
            return simpleRows;
        }

        public ArrayList<String> getLinks() {
            return links;
        }
    }
}
