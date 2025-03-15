package stud.atmt.atmtlite;

import java.util.ArrayList;
import java.util.List;

public class NTree<T> {
    private NJoint<T> root; // Корневой узел дерева

    /**
     * Конструктор пустого дерева
     */
    public NTree() {
        this.root = null;
    }

    /**
     * Конструктор дерева с корневым узлом
     * @param rootData данные корневого узла
     */
    public NTree(T rootData) {
        this.root = new NJoint<>(rootData);
    }

    /**
     * Получение корневого узла
     * @return корневой узел
     */
    public NJoint<T> getRoot() {
        return root;
    }

    /**
     * Установка корневого узла
     * @param rootData данные корневого узла
     */
    public void setRoot(T rootData) {
        this.root = new NJoint<>(rootData);
    }

    /**
     * Добавление потомка к указанному узлу
     * @param parentNode родительский узел
     * @param childData данные потомка
     */
    public void addChild(NJoint<T> parentNode, T childData) {
        NJoint<T> childNode = new NJoint<>(childData);
        parentNode.AddLink(childNode);
    }

    /**
     * Удаление последнего потомка у указанного узла
     * @param parentNode родительский узел
     */
    public void removeLastChild(NJoint<T> parentNode) {
        parentNode.DelLink();
    }

    /**
     * Поиск узла по данным
     * @param data данные для поиска
     * @return узел с данными или null, если не найден
     */
    public NJoint<T> findNode(T data) {
        return findNodeRecursive(root, data);
    }

    private NJoint<T> findNodeRecursive(NJoint<T> currentNode, T data) {
        if (currentNode == null) {
            return null;
        }
        if (currentNode.data.equals(data)) {
            return currentNode;
        }
        for (NJoint<T> child : currentNode.Links) {
            NJoint<T> foundNode = findNodeRecursive(child, data);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    /**
     * Итерация по дереву (обход в глубину)
     * @return список всех узлов в порядке обхода
     */
    public List<NJoint<T>> iterateTree() {
        List<NJoint<T>> nodes = new ArrayList<>();
        iterateTreeRecursive(root, nodes);
        return nodes;
    }

    private void iterateTreeRecursive(NJoint<T> currentNode, List<NJoint<T>> nodes) {
        if (currentNode == null) {
            return;
        }
        nodes.add(currentNode); // Добавляем текущий узел
        for (NJoint<T> child : currentNode.Links) {
            iterateTreeRecursive(child, nodes); // Рекурсивно обходим потомков
        }
    }

    /**
     * Получение всех тупиковых узлов (узлов без потомков)
     * @return список тупиковых узлов
     */
    public List<NJoint<T>> getLeafNodes() {
        List<NJoint<T>> leafNodes = new ArrayList<>();
        getLeafNodesRecursive(root, leafNodes);
        return leafNodes;
    }

    private void getLeafNodesRecursive(NJoint<T> currentNode, List<NJoint<T>> leafNodes) {
        if (currentNode == null) {
            return;
        }
        if (currentNode.Links.isEmpty()) {
            leafNodes.add(currentNode); // Если у узла нет потомков, это тупиковый узел
        } else {
            for (NJoint<T> child : currentNode.Links) {
                getLeafNodesRecursive(child, leafNodes); // Рекурсивно обходим потомков
            }
        }
    }

    /**
     * Вывод дерева в консоль
     */
    public void printTree() {
        printTreeRecursive(root, 0);
    }

    private void printTreeRecursive(NJoint<T> currentNode, int level) {
        if (currentNode == null) {
            return;
        }
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(currentNode.data);
        for (NJoint<T> child : currentNode.Links) {
            printTreeRecursive(child, level + 1);
        }
    }
}