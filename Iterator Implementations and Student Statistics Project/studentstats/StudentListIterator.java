
package studentstats;

import java.util.NoSuchElementException;
import itertools.DoubleEndedIterator;
import studentapi.*;

// A double ended iterator for iterating over an array of student records
class StudentPageIterator implements DoubleEndedIterator<Student> {
    // Array of student records
    private Student[] students;

    // Index of the current student
    private int currentIndex;

    // Index of the last student in the array
    private int lastIndex;

    // Constructor to initialize the iterator with an array of student records
    public StudentPageIterator(Student[] students) {
        this.students = students;
        this.currentIndex = 0;
        this.lastIndex = students.length - 1;
    }

    // Checks if there is another student to iterate over
    @Override
    public boolean hasNext() {
        return currentIndex <= lastIndex;
    }

    // Retrieves the next student and advances the iterator
    @Override
    public Student next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return students[currentIndex++];
    }

    // Retrieves the previous student and moves the iterator backwards
    @Override
    public Student reverseNext() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return students[lastIndex--];
    }
}


/**
 * A (double ended) iterator over student records pulled from the student API.
 *
 * <p>This does not load the whole student list immediately, but rather queries the API ({@link
 * StudentList#getPage}) only as needed.
 */
public class StudentListIterator implements DoubleEndedIterator<Student> {
    // TASK(8): Implement StudentListIterator: Add any fields you require
    // The API interface for accessing student records
    private StudentList students;

    // Total number of students in the list
    private int totalStudents;

    // Number of students already retrieved
    private int retrievedStudents;

    // Number of retries allowed for API queries
    private int retries;

    // Indices tracking the front and back pages
    private int front;
    private int back;

    // Iterator for the front page
    StudentPageIterator frontpage;

    // Iterator for the back page
    StudentPageIterator backpage;

    /**
     * Construct an iterator over the given {@link StudentList} with the specified retry quota.
     *
     * @param list The API interface.
     * @param retries The number of times to retry a query after getting {@link
     *     QueryTimedOutException} before declaring the API unreachable and throwing an {@link
     *     ApiUnreachableException}.
     */
    public StudentListIterator(StudentList list, int retries) {
        // TASK(8): Implement StudentListIterator
        this.students = list;
        this.totalStudents = list.getNumStudents();
        this.retries = retries;

        // Initialize iterators for front and back pages
        this.frontpage = new StudentPageIterator(getNewPage(0));
        this.backpage = new StudentPageIterator(getNewPage(list.getNumPages() - 1));
        this.front = 1;
        this.back = list.getNumPages() - 2;
    }

    /**
     * Construct an iterator over the given {@link StudentList} with a default retry quota of 3.
     *
     * @param list The API interface.
     */
    public StudentListIterator(StudentList list) {
        // TASK(8): Implement StudentListIterator
        // Use default retry quota of 3
        this(list, 3);
    }

    // Retrieves a new page of student records from the API, handling retries
    private Student[] getNewPage(int pageNum) throws ApiUnreachableException {
        int retriesCounter = 0;
        while (retriesCounter < retries) {
            try {
                return students.getPage(pageNum);
            } 
            catch (QueryTimedOutException e) {
                retriesCounter++;
            }
        }
        throw new ApiUnreachableException();
    }

    // Checks if there are more student records to retrieve
    @Override
    public boolean hasNext() {
        // TASK(8): Implement StudentListIterator
        if (retrievedStudents < totalStudents) {
            if (!frontpage.hasNext()) {
                frontpage = new StudentPageIterator(getNewPage(front));
                front++;
            }
            return frontpage.hasNext();
        }
        return false;
    }

    // Retrieves the next student record
    @Override
    public Student next() {
        // TASK(8): Implement StudentListIterator
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        retrievedStudents++;
        return frontpage.next();
    }

    // Retrieves the next student record in reverse order
    @Override
    public Student reverseNext() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!backpage.hasNext()) {
            backpage = new StudentPageIterator(getNewPage(back));
            back--;
        }
        retrievedStudents++;
        return backpage.reverseNext();
    }
}
