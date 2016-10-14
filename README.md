#Advanced Programming Techniques#

##Assignment 1##

**Refactoring**

The focus of this assignment is the practical application of refactoring to some existing object-oriented code. Find or create a running program that is poorly structured and can be improved by refactoring. Ideally, this will be code that you yourself have written in the past. Find code that can be subjected to at least five refactorings, where each of the refactorings is different from the others, and where at least two of the refactorings involve substantial changes to the internal design of the system. Since the course has used Java examples and the JUnit testing framework, it would be best if the program to refactor was written in the Java language, but a program written in another object-oriented language is possible, provided that you can complete all requirements of the assignment (in particular, that you use a framework to do unit testing).

Following the principles outlined in the course, apply at least five refactorings to your code. You must use version control (most likely SVN, but other version control systems are possible) to keep track of all the refactorings you do. Make sure you document and commit each refactoring using the version control system, and make sure you can extract any version of your code from the repository. You must also do unit testing as you do the refactoring. It is likely you will add or modify tests as you refactor your code into smaller methods. The testing code must also be kept under version control.

Write a brief formal report that describes how you did your refactoring. This should explain what you did for each refactoring, answering the following questions:

What needed to be improved? That is, what "*bad code smell*" was detected? Use the terminology found in Chapter 3 of the Fowler text.

What refactoring was applied? What steps did you follow? Use the terminology and mechanics outlined in the Fowler text, and illustrate the process with well-chosen code fragments taken from particular versions of your system.

How was the code tested?

Why is the code better structured after the refactoring? Does the result of the refactoring suggest or enable further refactorings?

Use version numbers to cross-reference your code as you describe each test. The report should include complete listings of the first and last versions of your program, as well as a complete listing of the final version of your test code. For intermediate versions of your program, print out only what is necessary to show the changes you made. To show that you have used version control in a comprehensive manner throughout the process, also print out the log (history) for each file (i.e. use the `svn log` command).

**Bonus: In-class Presentations**

Prepare a 5-minute presentation that explains the refactorings you applied to your code. Since this is not much time, you might want to highlight the more interesting changes you made. Effective presentations will most likely use presentation materials like handouts, transparencies, or PowerPoint slides.

Presentations will be done in the tutorials. Because time is limited, it is possible not everyone will get a chance to present his or her work. You are strongly encouraged to volunteer to do a presentation (it�s like the code reviews that are often done in industry). If, on the presentation day, there are not enough volunteers, the TA will pick students at random to do a presentation, so be prepared for this. Anyone who does a satisfactory presentation will get bonus marks.

Submit the following using the Assignment 1 Dropbox in D2L:

An electronic copy of your report (in PDF or Word format).

An electronic copy of the first and last versions of your refactored code.

An electronic copy of the final version of your unit test code.

An electronic copy of your version control logs.

#Advanced Programming Techniques#
**Assignment 1 Grading**


    Version control (including log files)______ / 10

    Unit Tests	                         ______ / 10

    Refactorings (minimum of 5)	         ______ / 10

    Report                               ______ / 10

    Total                                ______ / 40	

                                         ______%

    Bonus: In-class presentation         ______% / 10%

    Total                                ______%