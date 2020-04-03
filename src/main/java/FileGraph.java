import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;

public class FileGraph implements Serializable {
    private final HashSet<String> invalidLinks = new HashSet<>();
    private final ArrayList<String> simpleRows = new ArrayList<>();
    private final HashMap<String, Integer> fileIndex = new HashMap<>();
    private final ArrayList<Node> fileNodes = new ArrayList<>();

    Optional<Integer> addFile(String path) {
        path = Utils.resolvePath(path);
        if (fileIndex.containsKey(path)) {
            return Optional.of(fileIndex.get(path));
        }
        Optional<Utils.FileContent> content = Utils.readFile(path);
        if (!content.isPresent()) {
            return Optional.empty();
        }

        Node node = new Node(path);
        int newNodeIndex = fileNodes.size();
        fileIndex.put(path, newNodeIndex);
        fileNodes.add(node);

        HashMap<String, Optional<String>> links = new HashMap<>();
        for (String link : content.get().getLinks()) {
            String[] args = link.split("\\s+");
            if (args.length != 3) {
                if (args.length > 1) {
                    invalidLinks.add(path + "::" + args[1]);
                    links.put(args[1], Optional.empty());
                } else if (args.length > 0) {
                    simpleRows.add(args[0]);
                }
                continue;
            }
            String newPath = Utils.resolvePath(args[2], path);
            if (!Paths.get(newPath).toFile().isFile()) {
                invalidLinks.add(path + "::" + args[1]);
                links.put(args[1], Optional.empty());
            }
            links.put(args[1], Optional.of(newPath));
        }

        for (String row : content.get().getSimpleRows()) {
            node.simpleRowsIndexes.add(simpleRows.size());
            simpleRows.add(row);
        }

        for (Map.Entry<String, Optional<String>> link : links.entrySet()) {
            Optional<String> nextPath = link.getValue();
            if (nextPath.isPresent()) {
                Optional<Integer> nextFileIdx = addFile(nextPath.get());
                node.links.put(link.getKey(), nextFileIdx.orElse(-1));
                if (nextFileIdx.isPresent() && !nextFileIdx.get().equals(newNodeIndex)) {
                    fileNodes.get(nextFileIdx.get()).includedFrom.add(newNodeIndex);
                    node.simpleRowsIndexes.addAll(fileNodes.get(nextFileIdx.get()).simpleRowsIndexes);
                }
            } else {
                node.links.put(link.getKey(), -1);
            }
        }

        return Optional.of(newNodeIndex);
    }

    public void updateLink(String filePath, String uid, String newFilePath) {
        if (!invalidLinks.contains(filePath + "::" + uid) || !fileIndex.containsKey(filePath)) {
            return;
        }
        Integer fromFileIndex = fileIndex.get(filePath);
        String newPath = "./" + Paths.get(filePath).getParent().resolve(newFilePath).normalize().toString();
        Optional<Integer> index = addFile(newPath);
        if (index.isPresent()) {
            fileNodes.get(index.get()).includedFrom.add(fromFileIndex);
            fileNodes.get(fromFileIndex).links.put(uid, index.orElse(-1));
            invalidLinks.remove(filePath + "::" + uid);
        }
    }

    public HashSet<String> getInvalidLinks() {
        return invalidLinks;
    }

    public ArrayList<String> getSimpleRows() {
        return simpleRows;
    }

    public List<String> getFilesContains(String line) {
        ArrayList<String> result = new ArrayList<>();
        int index = simpleRows.indexOf(line);

        if (index < 0) {
            return result;
        }

        boolean[] used = new boolean[fileNodes.size()];

        for (int iNode = 0; iNode < fileNodes.size(); iNode++) {
            if (!used[iNode] && fileNodes.get(iNode).simpleRowsIndexes.contains(index)) {
                Deque<Integer> stack = new ArrayDeque<>();
                stack.add(iNode);
                used[iNode] = true;
                while (!stack.isEmpty()) {
                    Node curr = fileNodes.get(stack.removeLast());
                    result.add(curr.fileName);
                    for (int iNext : curr.includedFrom) {
                        if (!used[iNext]) {
                            stack.addLast(iNext);
                            used[iNext] = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static class Node implements Serializable {
        private final String fileName;
        private final ArrayList<Integer> includedFrom = new ArrayList<>();
        private final HashSet<Integer> simpleRowsIndexes = new HashSet<>();
        private final HashMap<String, Integer> links = new HashMap<>();

        public Node(String fileName) {
            this.fileName = fileName;
        }
    }

}
