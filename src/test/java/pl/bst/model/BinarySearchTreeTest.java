package pl.bst.model;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {
    private BinarySearchTree<Integer> sample(){var t=new BinarySearchTree<Integer>();for(int x:new int[]{5,3,7,2,4,6,8})t.insert(x);return t;}
    @Test void insertionUsesSetSemantics(){var t=sample();assertFalse(t.insert(5));assertEquals(7,t.size());assertTrue(t.search(4));assertFalse(t.search(99));}
    @Test void preOrderKeepsRootLeftRightOrder(){assertEquals(List.of(5,3,2,4,7,6,8),sample().preOrder());}
    @Test void deletingLeafRetainsOtherValues(){var t=sample();assertTrue(t.delete(2));assertFalse(t.search(2));assertEquals(6,t.size());assertTrue(t.search(3));}
    @Test void deletingNodeWithOneChildPromotesTheChild(){var t=sample();t.delete(2);t.delete(3);assertEquals(List.of(5,4,7,6,8),t.preOrder());}
    @Test void deletingRootWithTwoChildrenKeepsBSTOrdering(){var t=sample();assertTrue(t.delete(5));assertEquals(List.of(6,3,2,4,7,8),t.preOrder());assertFalse(t.search(5));}
    @Test void deletingOnlyRootAndMissingValueAreHandled(){var t=new BinarySearchTree<Integer>();t.insert(3);assertFalse(t.delete(4));assertTrue(t.delete(3));assertNull(t.snapshot());assertEquals(0,t.size());}
    @Test void snapshotRemainsUnchangedAfterMutation(){var t=sample();var before=t.snapshot();t.delete(3);assertEquals(3,before.left().value());assertEquals(4,t.snapshot().left().value());}
    @Test void clearResetsSizeAndSnapshot(){var t=sample();t.clear();assertTrue(t.preOrder().isEmpty());assertNull(t.snapshot());assertEquals(0,t.size());}
    @Test void doubleAndUnicodeStringTypesUseNaturalOrder(){var d=new BinarySearchTree<Double>();d.insert(1.5);d.insert(-2.0);assertTrue(d.search(-2.0));
        var s=new BinarySearchTree<String>();s.insert("żółw");s.insert("ala");assertEquals(List.of("żółw","ala"),s.preOrder());}
    @Test void nullCannotEnterTheTree(){var t=new BinarySearchTree<Integer>();assertThrows(NullPointerException.class,()->t.insert(null));}
    @Test void deeplySkewedTreesDoNotOverflowTheJavaStack(){var t=new BinarySearchTree<Integer>();for(int i=0;i<5000;i++)t.insert(i);
        assertEquals(5000,t.preOrder().size());assertNotNull(t.snapshot());assertTrue(t.delete(0));assertTrue(t.search(4999));}
    @Test void concurrentInsertionsPreserveEveryDistinctValue()throws Exception{
        var t=new BinarySearchTree<Integer>();try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var tasks=new ArrayList<Future<?>>();for(int worker=0;worker<8;worker++){int offset=worker*100;
                tasks.add(pool.submit(()->{for(int i=0;i<100;i++)t.insert(offset+i);}));}
            for(var task:tasks)task.get(5,TimeUnit.SECONDS);
        }assertEquals(800,t.size());for(int i=0;i<800;i++)assertTrue(t.search(i));
    }
}
