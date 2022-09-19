---
title: "A First Look | Lesson 1"
type: "docs"
date: 2019-09-18T13:06:27-06:00
---

# Lesson 1: A First Look

In today's technology landscape, the web is king. Web apps are everywhere, and the _lingua franca_ of the web is JavaScript. Whether the task is adding interactivity to a simple web page, creating a complex single-page application, or even writing microservices, JavaScript is the defacto tool. Despite its age, JavaScript has evolved to power an entire generation of web development. The JavaScript community is also one of the most active and prolific software development communities ever, with libraries and frameworks for any conceivable use.

---

**In this lesson:**

- What is ClojureScript?
- What makes ClojureScript unique?
- What sort of problems are easier to solve in ClojureScript than in JavaScript?

---

However, JavaScript is not without its warts. We need books to tell us what are the "Good Parts" and what parts we had best avoid. We have to deal with the reality of varying levels of support by different browsers (yes, even today). We need expend mental cycles deciding which of many viable UI frameworks we should use on our next project... and which framework we should switch to when we grow frustrated with the first framework we chose. While JavaScript has matured to meet many of the challenges of large-scale web development, there are times when another language is a better choice for a new project.

Over the course of this book, we will learn the ClojureScript programming language and see how it is especially well-suited to developing large single-page applications. While it may take a while to get used to all the parentheses, we'll see that this odd-looking language excels at building modular, high-performance user interfaces. Finally, we will see how the simple elegance of the language makes ClojureScript a joy to work with.

## Introducing ClojureScript

At the fundamental level, ClojureScript is a dialect of the Clojure programming language that compiles to JavaScript. Clojure was created in 2008 by Rich Hickey as a general-purpose programming language with the goal of being pragmatic, safe, and simple. While Clojure originally compiled only to Java Virtual Machine bytecode, ClojureScript entered the scene in 2011 as an option to bring Clojure to client side web development. While there are a few differences between Clojure and ClojureScript, they are largely the same language running on different platforms. ClojureScript inherits Clojure's pragmatism, safety, and simplicity.

ClojureScript has all the buzzwords of an obscure, academic language - immutable data structures, functional programming, Lisp, etc. - but that should not fool us into thinking that it is a language designed for academia. It is an intensely practical language that was born to address some of the issues that we as JavaScript programmers find most troubling. ClojureScript specifically addresses those pain points that we run into when building and maintaining large applications. It has presented such successful solutions to asynchronous programming, state management, and higher-level abstractions that numerous JavaScript libraries have appeared that mimic certain features of ClojureScript. It is a practical language that is especially well-suited to client-side web development.

Beyond being a practical language, ClojureScript can be a very enjoyable language to write. The terseness of a language like ClojureScript is a breath of fresh air when we have grown so accustomed to writing the same boilerplate over and over again. Additionally, ClojureScript comes with a much more extensive standard library than JavaScript, so those simple tasks that require custom code or a third-party library can often be accomplished without ever leaving core ClojureScript.

While we will look at many features of ClojureScript that make it different from JavaScript, we should not think that it is a totally alien language. After the initial "parenthesis shock", we will see that its syntax is actually simpler than that of JavaScript. Let's take a look at a couple of examples of code translated from JavaScript to ClojureScript to get a feel for how the language is structured. Below we have an example of a JavaScript function call. Since JavaScript can be written in several different styles, we'll look at an objected oriented example as well as a functional example.

![Object-Oriented JavaScript function calls](/img/lesson1/oop-js-func.png)

_Object-Oriented JavaScript function calls_

This object-oriented style is very familiar to most JavaScript programmers and requires little explanation. Next, we'll look at the perhaps slightly less familiar functional style. This style is widely used in _lodash_ and similar libraries.

![Functional JavaScript function calls](/img/lesson1/func-js-func.png)

_Functional JavaScript function calls_

Next, let's look at a ClojureScript version of the same example. Notice that there are the same number of parentheses in the ClojureScript version as there were in the JavaScript versions. In fact, the only differences from the functional JavaScript code is that the left parenthesis is moved to the left and there is no comma between arguments.

![ClojureScript function call](/img/lesson1/cljs-func.png)

_ClojureScript function call_

While this is a trivial example, it should be enough to see that ClojureScript should not be intimidating - different, yes, but not frightening. As we will see over the coming lessons, we need to adjust our eyes to read ClojureScript, but the process is not that different from learning a new library or programming technique.

### Quick Review

- Does ClojureScript or JavaScript come with a more extensive standard library?
- Does ClojureScript encourage an object-oriented style or a functional style
  like _lodash_ and _ramda_?

## ClojureScript's Sweet Spots

While ClojureScript is a general-purpose programming language, it is not the best tool for every job. If we just want to animate one or two elements on a webpage or implement an analytics snippet, ClojureScript is probably overkill (in fact, even jQuery may be overkill for such simple examples). How are we to decide, then, when to use ClojureScript and when to stick with JavaScript? In order to decide whether to use ClojureScript on a project, we should have an idea of the types of projects in which it excels.

### Writing Single-Page Applications

Clojure started out as a general-purpose application programming language for the JVM, so ClojureScript's heritage is based in application programming. Indeed we see that the constructs that make ClojureScript so valuable are precisely those that are necessary for application-type programs. Specifically, ClojureScript addresses JavaScript's issues that start as minor annoyances and escalate to major issues as an application grows. Anyone who has maintained a large JavaScript application knows how difficult it is to address strategic architecture, module loading, cross-browser compatibility, library selection, tooling, and a whole host of other issues simultaneously.

The problem with JavaScript is that each of these issues must be addressed separately, but your choice for solving one issue may affect others. For instance, the module system that we use is a separate concern from our build tool, which in turn is separate from our testing framework. However, we need to make sure that our build tool supports our testing framework, and both support our module system or can be easily integrated with it. Suddenly, the awesome app that we were planning to write gets stifled by the fact that we just spent 3 days trying to get the build set up. I can tell you that scenarios like this are commonplace, since I have experienced a number of them personally.

Paridoxically, ClojureScript makes things easier by taking away choices. The module system is built in to the language. There is a built-in test framework. Most libraries provide an API that works on common data structures in a functional style, so they are simple to integrate. Additionally, the Google Closure library that is built in will cover most common concerns such as handling dates, DOM manipulation, HTML5 history, graphics, and ajax. While building a ClojureScript application is not nearly the adventure that building a JavaScript one is, it is certainly more productive.

### Optimizing UIs

We have alluded to the fact that ClojureScript's immutable data structures make some interesting UI optimizations possible, but we have not gone into detail as to how that works. It is really the combination of React's virtual DOM concept and ClojureScript's immutable data structures that make such optimizations possible. Since we know that ClojureScript's data structures are immutable, we know that any structure that we create cannot change. If we have some data structure backing a UI component, we know that we will not need to re-render the component as long as it is backed by the same data structure. This knowledge allows us to create highly optimized UIs.

Consider this: we are writing a contact management app, and we have a `ContactList` component that contains `ContactListItem` components. These components are all backed by a list of contacts and should re-render whenever a contact changes. If we were writing the component using a JavaScript framework, we would either have to put our data inside special objects that the framework provides so that it can track changes, use a dirty-checking mechanism to periodically find what we need to change, or render everything to an in-memory representation of the DOM and render any changes to the actual DOM. The ClojureScript community has adopted the last method, but the story is actually better in ClojureScript, because we can be selective about which components we even need to render to the virtual DOM, saving additional CPU cycles.

![Optimizing a UI with immutable data structures](/img/lesson1/ui-optimization-tree.png)

_Optimizing a UI with immutable data structures_

In this example, whenever a `contact` is changed, we replace the map modeling that contact entirely. When it comes time to render to the virtual DOM, the `ContactList` is going to re-render, because the `contacts` list is now a new object entirely. Of the `ContactListItem` components, only the one that that reflects the contact we edited is going to re-render. The rest of the `ContactListItem`s can quickly see that their underlying data has not changed, so there is no work to be done. Furthermore, none of the other portions of the application need to render either. While this optimization may sound rather minor, we will see later that it can have a dramatic effect on the performance of an application.

### Modernizing Async

JavaScript has now adopted `async/await` - which is a first-class syntax for dealing with promise-like objects - as the preferred way to achieve asynchronous programming. You will still find raw promises, callbacks, and generators in some places, but `async/await` has become more or less universal.

ClojureScript, on the other hand, has embraced a style of asynchronous programming called CSP, or _Communicating Sequential Processes_. This is the same style of async that has proven so effective in the Go programming language. Using CSP, we do not deal directly with promises or callbacks. Instead, we think about values and passing them around via _channels_. For now, you can think of channels as streams or promises that can deliver more than one value. Additionally, we can write asynchronous code that looks like synchronous code, tremendously reducing the cognitive load of writing async code. Performing requests or getting input sequentially or in parallel are both natural. Some ClojureScript developers consider async the single most important advantage that ClojureScript has over JavaScript. We will have to judge for ourselves when we see it in action later in this book, but know that it enables a completely new way of thinking about async.

### Modularizing Design

In the early days of JavaScript, we probably wrote a single JavaScript file that we included in every page of a website that covered all of the scripting that we needed on the site. If the file got too big or different pages had entirely different requirements, we probably wrote several JavaScript files and included them on the applicable pages. Maybe eventually we heard about the "Module Pattern" or "Revealing Module Pattern" and separated our code into narrowly focused modules with one file per module. Now we had to worry about loading every file in the correct order on the page so that we would not try to reference a module that did not yet exist. At this point, we probably heard talk of module loaders that could asynchronously load only the modules we needed and figure out the correct order to load them in - they could even concatenate all of our modules into a single file for deployment. The problem was that there were once again several competing standards for module loading - AMD, CommonJS, and ES2015. Even today, finding the right tooling to integrate modules into our process can be painful, and every team needs at least one Webpack expert who is aware of the gotchas of bundling code for deployment.

ClojureScript, on the other hand, has the advantage of being a compiled language and can provide its own module system with no additional complexity. ClojureScript uses _namespaces_, which are named collections of functions and data, to organize code. Loading order, preventing circular dependencies, and compiling to a single asset for production are all part of the standard compiler toolchain. As an added benefit, the ClojureScript compiler outputs Google Closure modules, which it then passes off to the Google Closure compiler for additional optimization, including elimination of dead code paths. Having a good module system at the language level tremendously simplifies the setup process of any new project.

### Quick Review

- Which of the following projects would be a good fit for ClojureScript?
  - single-page app such as a dashboard for a CMS
  - adding animations to a static page
  - web-based game with complex asynchronous user interactions
  - CPU-intensive number-crunching simulations
- Does ClojureScript use the same module systems as JavaScript (CommonJS,
  and ES2015)?

## ClojureScript 101

Now that we have seen some of the advantages that ClojureScript can bring to front-end web development, let's take a step back and survey ClojureScript's distinct features. As with any technology that promises to bring significant improvement to the way we code, there will be new concepts. And as with any new concept, the first step towards mastery is familiarity. Let's get ready to explore what makes ClojureScript tick.

### A Compile-to-JavaScript Language

In 2008, if we were to do any client-side web programming, the only viable option was JavaScript. Over the next few years, languages that compiled to JavaScript started to appear. These languages either cleaned up JavaScript's cruft or added some features that were not present in JavaScript itself. Some of these languages were modest in their approach, retaining much of the feel of JavaScript. Others were radical departures from JavaScript that fell into the category of research languages. ClojureScript made significant improvements to JavaScript while sustaining the community support required of a language intended for professional use.

In addition to the other languages that compile to JavaScript, we must consider the fact that many of us are compiling newer versions of JavaScript to older versions so that we can take advantage of language features that make JavaScript more productive and enjoyable before they are supported by the major browsers. Starting with the ES2015 standard, JavaScript has accumulated many of the best ideas from more recent programming languages, but since new features are always introduced quicker than browsers can adopt them, we are perpetually at least a year away from using "Modern JavaScript", and we must unfortunately treat JavaScript itself as a compile-to-js language! In many fields, this sort of complexity would be considered insanity, but in web development, this is the status quo. In contrast to the constant flux of JavaScript, ClojureScript has remained remarkably stable as a language, with much of the innovation happening in libraries rather than the language itself.

As with any compile-to-js language, the fact that ClojureScript exists is a statement that JavaScript is not sufficient. CoffeeScript addressed JavaScript's verbose and inconsistent syntax (it was written in just over a week, after all). TypeScript, Dart, and PureScript address it's lack of a type system, enabling developers to better reason about their code. JavaScript itself addresses the age of the language, bringing more modern features while maintaining some semblance to previous versions and providing an easy path to migrate old JavaScript applications. ClojureScript brings a simpler syntax, an arsenal of data structures that rule out a whole class of bugs, a better paradigm for asynchronous programming, and excellent integration with one of the most popular UI frameworks (React). In short, ClojureScript attempts to be a better general-purpose front-end language than JavaScript; and the larger the application, the more its benefits will be evident.

### A Simple Language

JavaScript is a chameleon language. Not only is it possible to write code in imperative, object-oriented, or functional style; it is possible to mix all of these styles in the same codebase. Even if we consider a task as simple as iterating over an array, there are quite a few methods to accomplish this, all of them fairly idiomatic in JavaScript. If we are most comfortable with the imperative style, we could use a `for` loop and manually access each element of the array. On the other hand, we could use the `Array.prototype.forEach()` function (provided we do not have to worry about supporting old browsers). Finally, if we were already using _lodash_ on a project, we could use one of its helper functions. Each of these methods are demonstrated below, and they should look familiar to most JavaScript programmers.

```javascript
const numbers = [4, 8, 15, 16, 23, 42];

for (let num of numbers) {
  // <1>
  console.log(`The number is ${num}`);
}

numbers.forEach(
  // <2>
  (num) => console.log(`The number is ${num}`)
);

const printNum = (num) => {
  // <3>
  console.log(`The number is ${num}`);
};
_.each(numbers, printNum);
```

_Iterating over an array in JavaScript_

1. Imperative
2. Object-oriented
3. Functional

Perhaps more problematic than allowing several styles of programming to coexist in the same codebase is JavaScript's "bad parts" - the quirks that are the subject of so many technical interview questions. When a developer first learns JavaScript, there are a number of pitfalls that she must learn to avoid. Somehow, we have learned to live with all of the additional complexity laid upon us by JavaScript because we have not had the luxury of choosing a simpler language. Consider this partial list of some of JavaScripts quirks and think whether we would be better off adopting a language without so many gotchas:

- variable hoisting
- several ways to set `this`
- `==` vs `===`
- the `void` operator
- `'ba' + + 'n' + 'a' + 's'`
- What does `xs.push(x)` return? What about `xs.concat([x])`?

When we consider all of JavaScript's complexity, we can see that we must code very cautiously or risk being bitten by one of these quirks. For some simple applications, we may be able to live with this, but as our codebases grow, the value of a simpler language becomes more and more apparent. Maintaining a consistent codebase without loads of unnecessary complexity takes a great deal of skill and discipline. While there are a lot of expert JavaScript developers out there who do have the requisite skill and discipline, it does not change the fact that it is **hard** to write good JavaScript at the application level. Thankfully, ClojureScript is a simpler option - admittedly with a learning curve - but it is generally the things with a steeper learning curve that ultimately prove the most valuable.

Whereas we have seen that JavaScript promotes a wide variety of programming styles, ClojureScript is opinionated and is designed to make the functional style of programming easy. In fact, we will see that idiomatic ClojureScript looks a great deal like JavaScript written in the functional style, but with less ceremony. Below is an example of how you could iterate over a vector, which is similar to a JavaScript array.

```clojure
(def numbers [4, 8, 15, 16, 23, 42])

(doseq [n numbers]
  (println "The number is" n))
```

_Iterating over a vector in ClojureScript_

Like the JavaScript code, this defines a sequence of numbers then logs a statement to the console for each of the numbers. It even looks pretty similar to the object-oriented version with the exception that `doseq` is not attached to a particular object prototype. However, this - along with some minor variations - is how you can expect it to look when you need to iterate over a collection in ClojureScript. Always.

### A Powerful Language

One of the spectrums in programming languages is that of how much functionality to include by default. At one extreme is assembly, which translates directly into CPU instructions and has no "standard library", and at the other end is highly-specialized languages that include everything necessary to accomplish most any given task in their problem domain. When it comes to front-end web programming languages, JavaScript leans more towards the spartan end of the spectrum, and ClojureScript leans toward the "batteries included" end, providing higher level tools by default. Between its variety of core data structures and an extensive collection API, macros that allow for extension of the language itself, and the entire Google Closure library available by default, ClojureScript provides more powerful tools for constructing applications.

![Spectrum of programming languages](/img/lesson1/lang-spectrum.png)

_Spectrum of programming languages_

The abstractions provided by ClojureScript are higher-level than those provided by JavaScript, enabling most code to be written more concisely and descriptively. While JavaScript provides numbers, strings, arrays, objects, and simple control structures, ClojureScript provides similar primitives as well as keywords, lists, vectors, sets, maps, protocols, records, and multimethods. Don't worry if you have no idea what any of these things are - after all, that is what this book is all about! While the additional tools mean that there are more things to learn, it also means that there are fewer occasions to learn a new library or write our own data structures and generic algorithms.

### A Functional Language

Love it or hate it, ClojureScript embraces the concept of functional programming. If "functional programming" sounds like an intimidating, academic topic, do not fear - we'll see that most of the functional programming concepts should be at least somewhat familiar for those of us who work with JavaScript on a regular basis. This should not be surprising, since JavaScript was heavily influenced by Scheme (a functional Lisp, just like ClojureScript). Functional programming is one of the three main styles of programming supported by JavaScript, with an emphasis on using functions in the mathematical sense of a mapping of some input value to some output value.

| Paradigm        | Description                                                                                                            | Key Concepts                                                     |
| --------------- | ---------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- |
| Imperative      | Describes a program as a sequence of statements that may modify the program state, receive input, or produce output.   | Variables, loops, assignment, statements, subroutines            |
| Object-Oriented | Models the real world in terms of objects, their behaviors, and their interactions with each other.                    | Objects, classes or prototypes, methods, messages, object graphs |
| Functional      | Describes a program as a transformation of some input value to some output value using functions that can be composed. | Pure functions, immutable values, higher-order functions         |

_Comparison of JavaScript programming paradigms_

While functional programming in JavaScript is gaining momentum, the majority of code that we are likely to find is either imperative or object-oriented. Without getting too far into the nitty-gritty of functional programming at this point, we can say that ClojureScript focuses on building programs by assembling small functions together that take some data and return some new data without modifying the arguments that were passed in or any global state.

One key feature of writing functions this way is that when you call a function with the same arguments, you always get the same result. While this may seem like an unimportant property for a function, it makes testing and debugging much easier. If most of a program is written as pure functions, tests can be written without any set-up. Contrast this with the typical way that object-oriented systems are tested: a number of objects must be constructed and put in to just the right state before every test, or the test will not run correctly.

### Quick Review

- Is the ClojureScript language stable? Why or why not?
- List at least 3 ways in which ClojureScript improves upon JavaScript
- What is the difference between _simplicity_ and _familiarity_? What are some
  aspects of JavaScript that are not simple?
- Does ClojureScript or JavaScript operate at a higher level of abstraction?
- Of the 3 styles of programming that are common in JavaScript (imperative,
  object-oriented, and functional), which is encouraged by ClojureScript?

## Summary

ClojureScript is an incredible useful language, particularly for front-end web development. It shares many of JavaScript's functional programming concepts, but it is both a simpler and more productive language. ClojureScript may appear foreign with all its parentheses, but under the parenthesis-packed surface, it shares much in common with JavaScript. We should now understand:

- What ClojureScript is and what sets it apart from JavaScript
- What types of apps are the best fit for ClojureScript
