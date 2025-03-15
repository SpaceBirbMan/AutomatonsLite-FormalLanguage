package stud.atmt.atmtlite;

import java.util.ArrayList;

/**
 * Класс узла n-арного дерева
 * @param <T>
 */
public class NJoint<T> {
    public T data;
    public ArrayList<NJoint<T>> Links = new ArrayList<>(0); //указатели на другие узлы

    /**
     * Конструктор узла с данными (лист)
     * @param d
     */
    public NJoint(T d) {
        this.data = d;
    }

    /**
     * Добавление потомка
     * @param L
     */
    public void AddLink(NJoint<T> L) {
        Links.addLast(L);
    }

    /**
     * Удаление потомка
     */
    public void DelLink() {
        Links.removeLast();
    }

}