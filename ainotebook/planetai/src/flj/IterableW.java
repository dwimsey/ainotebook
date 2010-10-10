package flj;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A wrapper for Iterable that equips it with some useful functions.
 */
public final class IterableW<A> implements Iterable<A> {

  private final Iterable<A> i;

  private IterableW(final Iterable<A> i) {
    this.i = i;
  }

  /**
   * Wraps the given iterable.
   *
   * @param a The iterable to wrap.
   * @return An iterable equipped with some useful functions.
   */
  public static <A> IterableW<A> wrap(final Iterable<A> a) {
    return new IterableW<A>(a);
  }

  /**
   * Provides a function that wraps the given iterable.
   *
   * @return A function that returns the given iterable, wrapped.
   */
  public static <A, T extends Iterable<A>> F<T, IterableW<A>> wrap() {
    return new F<T, IterableW<A>>() {
      public IterableW<A> f(final T a) {
        return wrap(a);
      }
    };
  }

  /**
   * Returns an Iterable that completely preserves the argument. The unit function for Iterables.
   *
   * @param a A value to preserve in an Iterable.
   * @return An Iterable that yields the argument when iterated over.
   */
  public static <A> IterableW<A> iterable(final A a) {
    return wrap(Option.some(a));
  }

  /**
   * Wraps a given function's return value in a Iterable.
   * The Kleisli arrow for Iterables.
   *
   * @param f The function whose return value to wrap in a Iterable.
   * @return The equivalent function whose return value is iterable.
   */
  public static <A, B> F<A, IterableW<B>> iterable(final F<A, B> f) {
    return new F<A, IterableW<B>>() {
      public IterableW<B> f(final A a) {
        return iterable(f.f(a));
      }
    };
  }

  /**
   * Provides a transformation from a function to a Iterable-valued function that is equivalent to it.
   * The first-class Kleisli arrow for Iterables.
   *
   * @return A transformation from a function to the equivalent Iterable-valued function.
   */
  public static <A, B> F<F<A, B>, F<A, IterableW<B>>> arrow() {
    return new F<F<A, B>, F<A, IterableW<B>>>() {
      public F<A, IterableW<B>> f(final F<A, B> f) {
        return iterable(f);
      }
    };
  }

  /**
   * Binds the given function across the wrapped Iterable with a final join.
   *
   * @param f A function to bind across the Iterable.
   * @return an iterable result of binding the given function over the wrapped Iterable.
   */
  public <B, T extends Iterable<B>> IterableW<B> bind(final F<A, T> f) {
    return wrap(Stream.iterableStream(this).bind(new F<A, Stream<B>>() {
      public Stream<B> f(final A a) {
        return Stream.iterableStream(f.f(a));
      }
    }));
  }

  /**
   * Performs function application within an iterable (applicative functor pattern).
   *
   * @param f The iterable function to apply.
   * @return A new iterable after applying the given iterable function to the wrapped iterable.
   */
  public <B> IterableW<B> apply(final Iterable<F<A, B>> f) {
    return wrap(f).bind(new F<F<A, B>, Iterable<B>>() {
      public Iterable<B> f(final F<A, B> f) {
        return map(f);
      }
    });
  }

  /**
   * Binds the given function to the values in the given iterables with a final join.
   *
   * @param a A given iterable to bind the given function with.
   * @param b A given iterable to bind the given function with.
   * @param f The function to apply to the values in the given iterables.
   * @return A new iterable after performing the map, then final join.
   */
  public static <A, B, C> IterableW<C> bind(final Iterable<A> a, final Iterable<B> b, final F<A, F<B, C>> f) {
    return wrap(b).apply(wrap(a).map(f));
  }

  /**
   * Promotes a function of arity-2 to a function on iterables.
   *
   * @param f The function to promote.
   * @return A function of arity-2 promoted to map over iterables.
   */
  public static <A, B, C> F<Iterable<A>, F<Iterable<B>, IterableW<C>>> liftM2(final F<A, F<B, C>> f) {
    return Function.curry(new F2<Iterable<A>, Iterable<B>, IterableW<C>>() {
      public IterableW<C> f(final Iterable<A> ca, final Iterable<B> cb) {
        return bind(ca, cb, f);
      }
    });
  }


  /**
   * The first-class bind function over Iterable.
   * Returns a function that binds a given function across a given iterable.
   *
   * @return a function that binds a given function across a given iterable.
   */
  public static <A, B, T extends Iterable<B>> F<IterableW<A>, F<F<A, T>, IterableW<B>>> bind() {
    return new F<IterableW<A>, F<F<A, T>, IterableW<B>>>() {
      public F<F<A, T>, IterableW<B>> f(final IterableW<A> a) {
        return new F<F<A, T>, IterableW<B>>() {
          public IterableW<B> f(final F<A, T> f) {
            return a.bind(f);
          }
        };
      }
    };
  }

  /**
   * Joins an Iterable of Iterables into a single Iterable.
   *
   * @param as An Iterable of Iterables to join.
   * @return the joined Iterable.
   */
  public static <A, T extends Iterable<A>> IterableW<A> join(final Iterable<T> as) {
    final F<T, T> id = Function.identity();
    return wrap(as).bind(id);
  }

  /**
   * Returns a function that joins an Iterable of Iterables into a single Iterable.
   *
   * @return a function that joins an Iterable of Iterables into a single Iterable.
   */
  public static <A, T extends Iterable<A>> F<Iterable<T>, IterableW<A>> join() {
    return new F<Iterable<T>, IterableW<A>>() {
      public IterableW<A> f(final Iterable<T> a) {
        return join(a);
      }
    };
  }

  /**
   * Maps a given function across the wrapped Iterable.
   *
   * @param f A function to map across the wrapped Iterable.
   * @return An Iterable of the results of mapping the given function across the wrapped Iterable.
   */
  public <B> IterableW<B> map(final F<A, B> f) {
    return bind(iterable(f));
  }

  /**
   * Returns a function that promotes any function so that it operates on Iterables.
   *
   * @return a function that promotes any function so that it operates on Iterables.
   */
  public static <A, B> F<F<A, B>, F<IterableW<A>, IterableW<B>>> map() {
    return new F<F<A, B>, F<IterableW<A>, IterableW<B>>>() {
      public F<IterableW<A>, IterableW<B>> f(final F<A, B> f) {
        return new F<IterableW<A>, IterableW<B>>() {
          public IterableW<B> f(final IterableW<A> a) {
            return a.map(f);
          }
        };
      }
    };
  }

  /**
   * The catamorphism for Iterables, implemented as a left fold.
   *
   * @param f The function with which to fold the wrapped iterable.
   * @param z The base case value of the destination type, applied first (leftmost) to the fold.
   * @return The result of the catamorphism.
   */
  public <B> B foldLeft(final F<B, F<A, B>> f, final B z) {
    B p = z;
    for (final A x : this) {
      p = f.f(p).f(x);
    }
    return p;
  }

  /**
   * Takes the first 2 elements of the iterable and applies the function to them,
   * then applies the function to the result and the third element and so on.
   *
   * @param f The function to apply on each element of the iterable.
   * @return The final result after the left-fold reduction.
   */
  public A foldLeft1(final F2<A, A, A> f) {
    return foldLeft1(Function.curry(f));
  }

  /**
   * Takes the first 2 elements of the iterable and applies the function to them,
   * then applies the function to the result and the third element and so on.
   *
   * @param f The function to apply on each element of the iterable.
   * @return The final result after the left-fold reduction.
   */
  public A foldLeft1(final F<A, F<A, A>> f) {
    return Stream.iterableStream(this).foldLeft1(f);
  }

  /**
   * The catamorphism for Iterables, implemented as a right fold.
   *
   * @param f The function with which to fold the wrapped iterable.
   * @param z The base case value of the destination type, applied last (rightmost) to the fold.
   * @return The result of the catamorphism.
   */
  public <B> B foldRight(final F2<A, B, B> f, final B z) {
    final F<B, B> id = Function.identity();
    return foldLeft(Function.curry(new F3<F<B, B>, A, B, B>() {
      public B f(final F<B, B> k, final A a, final B b) {
        return k.f(f.f(a, b));
      }
    }), id).f(z);
  }

  /**
   * Returns an iterator for this iterable.
   *
   * @return an iterator for this iterable.
   */
  public Iterator<A> iterator() {
    return i.iterator();
  }
  

  /**
   * Returns an iterator for this iterable.
   *
   * @return an iterator for this iterable.
   */
  @SuppressWarnings("unchecked")
  public <K, Z> Iterator<P2<K, Z>> iteratorType() {
    return (Iterator<P2<K, Z>>) this.iterator();
  }

  /**
   * Zips this iterable with the given iterable of functions, applying each function in turn to the
   * corresponding element in this iterable to produce a new iterable. The iteration is normalised
   * so that it ends when one of the iterators is exhausted.
   *
   * @param fs The iterable of functions to apply to this iterable.
   * @return A new iterable with the results of applying the functions to this iterable.
   */
  public <B> IterableW<B> zapp(final Iterable<F<A, B>> fs) {
    return wrap(Stream.iterableStream(this).zapp(Stream.iterableStream(fs)));
  }

  /**
   * Zips this iterable with the given iterable using the given function to produce a new iterable. If
   * this iterable and the given iterable have different lengths, then the longer iterable is normalised
   * so this function never fails.
   *
   * @param bs The iterable to zip this iterable with.
   * @param f  The function to zip this iterable and the given iterable with.
   * @return A new iterable with a length the same as the shortest of this iterable and the given
   *         iterable.
   */
  public <B, C> Iterable<C> zipWith(final Iterable<B> bs, final F<A, F<B, C>> f) {
    return wrap(Stream.iterableStream(this).zipWith(Stream.iterableStream(bs), f));
  }

  /**
   * Zips this iterable with the given iterable using the given function to produce a new iterable. If
   * this iterable and the given iterable have different lengths, then the longer iterable is normalised
   * so this function never fails.
   *
   * @param bs The iterable to zip this iterable with.
   * @param f  The function to zip this iterable and the given iterable with.
   * @return A new iterable with a length the same as the shortest of this iterable and the given
   *         iterable.
   */
  public <B, C> Iterable<C> zipWith(final Iterable<B> bs, final F2<A, B, C> f) {
    return zipWith(bs, Function.curry(f));
  }

  /**
   * Zips this iterable with the given iterable to produce a iterable of pairs. If this iterable and the
   * given iterable have different lengths, then the longer iterable is normalised so this function
   * never fails.
   *
   * @param bs The iterable to zip this iterable with.
   * @return A new iterable with a length the same as the shortest of this iterable and the given
   *         iterable.
   */
  public <B> Iterable<P2<A, B>> zip(final Iterable<B> bs) {
    return wrap(Stream.iterableStream(this).zip(Stream.iterableStream(bs)));
  }

  /**
   * Zips this iterable with the index of its element as a pair.
   *
   * @return A new iterable with the same length as this iterable.
   */
  public Iterable<P2<A, Integer>> zipIndex() {
    return wrap(Stream.iterableStream(this).zipIndex());
  }

  /**
   * Returns a java.util.List implementation for this iterable.
   * The returned list cannot be modified.
   *
   * @return An immutable implementation of java.util.List for this iterable.
   */
  public java.util.List<A> toStandardList() {
    return new java.util.List<A>() {

      public int size() {
        return Stream.iterableStream(IterableW.this).length();
      }

      public boolean isEmpty() {
        return Stream.iterableStream(IterableW.this).isEmpty();
      }

      @SuppressWarnings({"unchecked"})
      public boolean contains(final Object o) {
        return Stream.iterableStream(IterableW.this).exists(Equal.<A>anyEqual().eq((A) o));
      }

      public Iterator<A> iterator() {
        return IterableW.this.iterator();
      }

      public Object[] toArray() {
        return Array.iterableArray(Stream.iterableStream(IterableW.this)).array();
      }

      @SuppressWarnings({"SuspiciousToArrayCall"})
      public <T> T[] toArray(final T[] a) {
        return Stream.iterableStream(IterableW.this).toCollection().toArray(a);
      }

      public boolean add(final A a) {
        return false;
      }

      public boolean remove(final Object o) {
        return false;
      }

      public boolean containsAll(final Collection<?> c) {
        return Stream.iterableStream(IterableW.this).toCollection().containsAll(c);
      }

      public boolean addAll(final Collection<? extends A> c) {
        return false;
      }

      public boolean addAll(final int index, final Collection<? extends A> c) {
        return false;
      }

      public boolean removeAll(final Collection<?> c) {
        return false;
      }

      public boolean retainAll(final Collection<?> c) {
        return false;
      }

      public void clear() {
        throw new UnsupportedOperationException("Modifying an immutable List.");
      }

      public A get(final int index) {
        return Stream.iterableStream(IterableW.this).index(index);
      }

      public A set(final int index, final A element) {
        throw new UnsupportedOperationException("Modifying an immutable List.");
      }

      public void add(final int index, final A element) {
        throw new UnsupportedOperationException("Modifying an immutable List.");
      }

      public A remove(final int index) {
        throw new UnsupportedOperationException("Modifying an immutable List.");
      }

      public int indexOf(final Object o) {
        int i = -1;
        for (final A a : IterableW.this) {
          i++;
          if (a.equals(o))
            return i;
        }
        return i;
      }

      public int lastIndexOf(final Object o) {
        int i = -1;
        int last = -1;
        for (final A a : IterableW.this) {
          i++;
          if (a.equals(o))
            last = i;
        }
        return last;
      }

      public ListIterator<A> listIterator() {
        return toListIterator(toZipper());
      }

      public ListIterator<A> listIterator(final int index) {
        return toListIterator(toZipper().bind(Zipper.<A>move().f(index)));
      }

      public java.util.List<A> subList(final int fromIndex, final int toIndex) {
        return wrap(Stream.iterableStream(IterableW.this).drop(fromIndex).take(toIndex - fromIndex)).toStandardList();
      }

      private ListIterator<A> toListIterator(final Option<Zipper<A>> z) {
        return new ListIterator<A>() {

          private Option<Zipper<A>> pz = z;

          public boolean hasNext() {
            return pz.isSome() && !pz.some().atEnd();
          }

          public A next() {
            if (pz.isSome())
              pz = pz.some().next();
            else throw new NoSuchElementException();
            if (pz.isSome())
              return pz.some().focus();
            else throw new NoSuchElementException();
          }

          public boolean hasPrevious() {
            return pz.isSome() && !pz.some().atStart();
          }

          public A previous() {
            pz = pz.some().previous();
            return pz.some().focus();
          }

          public int nextIndex() {
            return pz.some().index() + (pz.some().atEnd() ? 0 : 1);
          }

          public int previousIndex() {
            return pz.some().index() - (pz.some().atStart() ? 0 : 1);
          }

          public void remove() {
            throw new UnsupportedOperationException("Remove on immutable ListIterator");
          }

          public void set(final A a) {
            throw new UnsupportedOperationException("Set on immutable ListIterator");
          }

          public void add(final A a) {
            throw new UnsupportedOperationException("Add on immutable ListIterator");
          }
        };
      }

    };
  }

  public Option<Zipper<A>> toZipper() {
    return Zipper.fromStream(Stream.iterableStream(this));
  }
}
