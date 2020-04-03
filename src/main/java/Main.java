import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private final static HashMap<String, Command> toCommand = new HashMap<>();

    static {
        toCommand.put(":save", Command.SAVE);
        toCommand.put(":load", Command.LOAD);
        toCommand.put(":add", Command.ADD_FILE);
        toCommand.put(":upd", Command.UPDATE_LINK);
        toCommand.put(":files", Command.GET_FILES_CONTAINS);
        toCommand.put(":lines", Command.GET_LIST_OF_LINES);
        toCommand.put(":inv", Command.GET_INVALID_LINKS);

    }

    private static String formatInvalidLinks(String link) {
        return link.replace("::", " -> ");
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);
        FileGraph graph = new FileGraph();

        while (scanner.hasNext()) {
            String[] args = scanner.nextLine().split("\\s+");
            Command cmd = toCommand.getOrDefault(args[0], Command.ERROR);
            switch (cmd) {
                case SAVE:
                    if (args.length == 1) {
                        printHelp();
                        break;
                    }
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[1]))) {
                        oos.writeObject(graph);
                    } catch (IOException e) {
                        System.out.println("Failed. " + e.toString());
                    }
                    break;
                case LOAD:
                    if (args.length == 1) {
                        printHelp();
                        break;
                    }
                    graph = new FileGraph();
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]))) {
                        graph = (FileGraph) ois.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Failed. " + e.toString());
                    }
                    break;
                case ADD_FILE:
                    if (args.length == 1) {
                        printHelp();
                        break;
                    }
                    graph.addFile(args[1]);
                    break;
                case UPDATE_LINK:
                    if (args.length < 4) {
                        printHelp();
                        break;
                    }
                    graph.updateLink(args[1], args[2], args[3]);
                    break;
                case GET_FILES_CONTAINS:
                    if (args.length == 1) {
                        printHelp();
                        continue;
                    }
                    graph.getFilesContains(args[1]).forEach(System.out::println);
                    break;
                case GET_INVALID_LINKS:
                    System.out.println("Invalid links:");
                    graph.getInvalidLinks().stream().map(Main::formatInvalidLinks).forEach(System.out::println);
                    break;
                case GET_LIST_OF_LINES:
                    System.out.println("Known lines:");
                    graph.getSimpleRows().forEach(System.out::println);
                    break;
                case ERROR:
                    printHelp();
            }
        }
    }

    public static void main(String[] args) {
        run();
    }

    public static void printHelp() {
        System.out.println("Usage:");
        for (Map.Entry<String, Command> cmd : toCommand.entrySet()) {
            if (!cmd.getValue().equals(Command.ERROR)) {
                System.out.printf("    %-10s", cmd.getKey());
                System.out.println(cmd.getValue().toString());
            }
        }
    }

    private enum Command {
        SAVE(" [FILE]   - save state to FILE"),
        LOAD(" [FILE]   - load state from FILE"),
        ADD_FILE(" [FILE]   - add FILE to structure"),
        UPDATE_LINK(" [FILE] [LINK] [NEW_FILE]    - fix LINK in FILE to NEW_FILE"),
        GET_FILES_CONTAINS(" [LINE]   - get list of files containing LINE"),
        GET_INVALID_LINKS("          - get list of invalid links"),
        GET_LIST_OF_LINES("          - get all simple rows"),
        ERROR("Something went wrong");

        private final String what;

        Command(String what) {
            this.what = what;
        }

        @Override
        public String toString() {
            return this.what;
        }
    }
}
