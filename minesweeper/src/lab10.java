import java.util.*;
import tester.Tester;

class Pair<L, R> {
  L left;
  R right;

  Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }
}

class IteratorUtils {
  // return an Iterator of pairs of corresponding elements.
  // If the lists are of different sizes, an exception should be thrown.
  <X, Y> Iterator<Pair<X, Y>> zipStrict(ArrayList<X> arr1, ArrayList<Y> arr2) {
    if (arr1.size() != arr2.size()) {
      throw new IllegalArgumentException("the lists are of different sizes!");
    }
    else {
      return new ZipStrict<X, Y>(arr1, arr2);
    }
  }

  // should return an Iterator of pairs of corresponding elements.
  // If the lists are of different sizes, only return pairs up to the size of the
  // shorter one.
  public <X, Y> Iterator<Pair<X, Y>> zipLists(ArrayList<X> arr1, ArrayList<Y> arr2) {
    return new ZipStrict<X, Y>(arr1, arr2);
  }

  // produce a new Iterator that concatenates the two Iterators
  public <T> Iterator<T> concat(Iterator<T> iter1, Iterator<T> iter2) {
    return null;
  }
}

class ZipStrict<X, Y> implements Iterator<Pair<X, Y>> {
  Iterator<X> arr1;
  Iterator<Y> arr2;

  ZipStrict(ArrayList<X> arr1list, ArrayList<Y> arr2list) {
    this.arr1 = arr1list.iterator();
    this.arr2 = arr2list.iterator();
  }

  // checks if it has a next
  public boolean hasNext() {
    return arr1.hasNext() && arr2.hasNext();

  }

  // returns the next
  public Pair<X, Y> next() {
    if (!this.hasNext()) {
      throw new NoSuchElementException("There are no more items");
    }
    else {
      return new Pair<X, Y>(arr1.next(), arr2.next());
    }
  }
}

class Concat<T> implements Iterator<T> {
  Iterator<T> iter1;
  Iterator<T> iter2;

  Concat(Iterator<T> iter1, Iterator<T> iter2) {
    this.iter1 = iter1;
    this.iter2 = iter2;
  }

  //
  public boolean hasNext() {
    return iter1.hasNext() || iter2.hasNext();
  }

  //
  public T next() {
    if (iter1.hasNext()) {
      return iter1.next();
    }
    else if (iter2.hasNext()) {
      return iter2.next();
    }
    else
      throw new NoSuchElementException("there are no more items");
  }
}

class ExamplesIterator {
  IteratorUtils iu = new IteratorUtils();
  ArrayList<Integer> mt = new ArrayList<Integer>(Arrays.asList());
  ArrayList<Integer> a1 = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
  ArrayList<Integer> a2 = new ArrayList<Integer>(Arrays.asList(4, 5, 6));
  ArrayList<Integer> a3 = new ArrayList<Integer>(Arrays.asList(1, 4, 2, 5, 3, 6));

  // test zipStrict
  boolean testZipStrict(Tester t) {
    Iterator<Pair<Integer, Integer>> z1 = this.iu.zipStrict(a1, a2);
    return t.checkExpect(z1.hasNext(), true)
        && t.checkExpect(z1.next(), new Pair<Integer, Integer>(1, 4))
        && t.checkExpect(z1.next(), new Pair<Integer, Integer>(2, 5))
        && t.checkExpect(z1.next(), new Pair<Integer, Integer>(3, 6));
  }

  boolean testException(Tester t) {
    return t.checkException(new IllegalArgumentException("the lists are of different sizes!"),
        this.iu, "zipStrict", a1, a3);
  }

  // test zipList
  boolean testZipList(Tester t) {
    Iterator<Pair<Integer, Integer>> z2 = this.iu.zipLists(a2, a3);
    return t.checkExpect(z2.hasNext(), true)
        && t.checkExpect(z2.next(), new Pair<Integer, Integer>(4, 1))
        && t.checkExpect(z2.next(), new Pair<Integer, Integer>(5, 4))
        && t.checkExpect(z2.next(), new Pair<Integer, Integer>(6, 2))
        && t.checkExpect(z2.hasNext(), false)
        && t.checkException(new NoSuchElementException("There are no more items"), z2, "next");
  }

  // tests concat
  boolean testConcat(Tester t) {
    Iterator<Integer> z3 = this.iu.concat(a1.iterator(), a2.iterator());
    return t.checkExpect(z3.hasNext(), true) 
        && t.checkExpect(z3.next(), 1)
        && t.checkExpect(z3.next(), 2) && t.checkExpect(z3.next(), 3) && t.checkExpect(z3.next(), 4)
        && t.checkExpect(z3.next(), 5) && t.checkExpect(z3.next(), 6)
        && t.checkExpect(z3.hasNext(), false);
  }

}
