---
title: "ClojureScript in the JavaScript Ecosystem | Lesson 2"
type: "docs"
date: 2019-09-18T13:06:27-06:00
---

# Lesson 2: ClojureScript in the JavaScript Ecosystem

Now that we have a good idea of what ClojureScript is and how to use it, we will
continue to pull back the curtain to get a clearer picture of how this curious
language fits into its environment - the JavaScript ecosystem. While the
language is quite different from JavaScript, it maintains a symbiotic
relationship to its JavaScript host. JavaScript needs ClojureScript, and
ClojureScript needs JavaScript. Let's explore this interesting symbiosis.

---

**In this lesson:**

- What problems in JavaScript does ClojureScript try to solve?
- How using a compiled language helps in application development
- Why is JavaScript an ideal platform for ClojureScript?

---

## Why JavaScript Needs Clojure

Having seen ClojureScript's sweet spots, it should be apparent that there are
some gains that it promises. Still, can we get a similar advantage from
JavaScript itself without having to learn a new language? Also, does
ClojureScript really give us that much additional leverage in our daily
development tasks? ClojureScript may not be the best tool for trivial tasks, but
for anything more complex, JavaScript does in fact _need_ a language like
Clojure to enable more productive and enjoyable development.

> **Clojure(Script)**
>
> You may have noticed several times where I have used the terms "Clojure" and
> "ClojureScript" interchangeably. Clojure as a language has implementations that
> compile to both Java bytecode and to JavaScript. Some of the potential confusion
> comes from the fact that "Clojure" refers to both the language and its Java
> implementation. I will follow the general pattern of the Clojure community of
> using the two terms interchangeably when talking about the language itself and
> using "ClojureScript" when discussing the ecosystem or language features that
> are specific to ClojureScript.

### Higher Level Language

ClojureScript operates with higher-level constructs than JavaScript. In
JavaScript, we work largely with variables, loops, conditional branching
structures, objects and arrays. In ClojureScript, we work with expressions,
collections, sequences, and transformations. The journey from lower-level
concepts to higher-level ones is the way that we gain productivity.

![Features defining each level of abstraction](/img/lesson2/language-hierarchy.png)

_Features defining each level of abstraction_

When we work at a higher level, a couple of interesting things happen. First,
it takes less code to accomplish a given task, which helps with both initial
development and debugging/maintenance. Second, it causes the structure of the
code more closely resemble the problem domain, making it clearer for us to
understand when we come back to it. Third, it frees us to think more about the
problems of the domain rather than technical implementation issues. All of these
factors can enable huge productivity boosts, both in the initial development and
maintenance phases of an application.

When we write less code to accomplish a given task, there are a couple of
benefits. First, it almost goes without saying that it is quicker to write a
little code than it is a lot of code. Even though more time is usually spent
designing and planning code than actually writing it, we do not want to be
hampered by how many keystrokes it takes to turn our ideas into code. Second,
fewer lines of code means fewer bugs. The developer who would rather spend her
time fixing bugs than writing new features is either a rarity or nonexistent.
The terseness of a high-level language like ClojureScript means that there are
fewer places for bugs to hide, and in turn, we can spend more time making
forward progress.

### Less Boilerplate

I cannot count the times that I have had a simple task that I wanted to
accomplish with JavaScript - say, performing a deep clone of an object - but had
to do a Google search to remember how to do it either using vanilla JavaScript
or the libraries that I had available. Usually, I would end up on some
StackOverflow thread that I had already visited numerous times and copying and
pasting the example into yet another "utils" file in yet another project.
Libraries such as _lodash_ and _jQuery_ help compensate for JavaScript's lack of
common utilities, but they do not solve the problem that one must look beyond
the language itself to get the functionality of a robust standard library.

The problem of needing to pull in third party libraries for most tasks is
uniquely problematic for the browser because every additional library adds time
to the page load. Compound this issue with the fact that most web apps at least
need to consider mobile clients with slow networks. When every byte counts, as
it does on the web, we are continually faced with the question of whether to
include another library for limited utility or write the functions that we need
from scratch.

Finally, JavaScript developers must continually face the reality of browser
compatibility issues. The available options are to target the lowest common
denominator of the browser that you would like to support (and miss out on the
language features that improve developer productivity), pull in libraries
(and add substantial page size), or implement browser-detection and write the
browser-specific portions from scratch (and face the additional complexity that
comes with browser hacks). The choices do not sound very attractive, and we
should not have to make a trade-off between developer productivity, performance,
and complexity. In order to solve the browser compatibility problem without
sacrificing any of these things, we need to look outside JavaScript itself.

ClojureScript, on the other hand, has a rich set of data structures and
functions for working with collections, strings, math, state management,
JavaScript interoperability, and more. Additionally, ClojureScript is built on
top of Google's Closure (with an "s", not a "j") library, putting the same tools
that power applications like Gmail and Google Docs at your fingertips. With so
many tools at our disposal, we'll see that the amount of utility code that we
need to write is minimal. Finally, ClojureScript compiles down to a
widely-supported subset of JavaScript, making browser compatibility much less of
an issue. ClojureScript takes the focus off the "plumbing", allowing us to focus
more on the interesting problems of the domain in which we are working.

### Immutable Data by Default

We have already looked at immutable data as one of the fundamental concepts of
functional programming. While much of the JavaScript community is starting to
recognize the value of immutable data, working with immutable data in JavaScript
is still not native and can feel somewhat cumbersome. Libraries like Facebook's
_Immutable.js_  allow us to get the benefits of immutable data from JavaScript,
but once again, the language currently has no native support.

In ClojureScript, however, the situation is reversed. All of the default data
structures are immutable, and we have to go out of our way to work with mutable
objects. This is one area where ClojureScript is very opinionated, but the style
of programming that it promotes is one that will lead to fewer bugs and - as we
have already seen - optimized user interfaces. Once we have become accustomed to
using ClojureScript's data structures, returning to mutable objects and arrays
will feel unusual - even dangerous.

### Compiler Optimized

One advantage that a compiled language has is that it can implement
optimizations in the JavaScript code that it produces. It is rare for a
high-level language to match either the speed, resource usage, or compiled
code size of a lower-level language. ClojureScript, however, can often produce
JavaScript that runs as fast as hand-written JavaScript. Its immutable data
structures do usually consume more memory and are slower than raw objects and
arrays, but the UI optimizations afforded by these data structures can make
ClojureScript interfaces _effectively faster_ than a corresponding JavaScript
interface.

One metric that matters a great deal to JavaScript programmers is code size.
When working in a server-side environment, the code size is usually not a
concern - the code is read from disk and immediately read into memory. However,
with front-end JavaScript applications, the code usually must be read over the
internet, potentially over a low-bandwidth mobile network. In this situation,
every byte counts, and we are used to laboring over our code and trying to
make it as small as possible, even at the cost of clarity. Minification helps
tremendously, but we still must be mindful about including more libraries.
Often, the benefit added by a library is offset by the kilobytes that it adds to
page load time.

One of the most interesting features of the ClojureScript compiler is that it
produces Google Closure modules, and it then makes use of the Closure Compiler
to optimize the JavaScript. Since the ClojureScript compiler guarantees that the
JavaScript it produces is valid Google Closure modules, we can safely make use
of the Closure Compiler's most aggressive optimizations when preparing
production assets. In addition to the typical removal of whitespace and renaming
variables, the Closure Compiler will analyze an entire codebase and remove any
code paths that can never be called. Effectively, this means that we can pull
in a large library, and if we use only a couple of functions from this library,
only those functions and the functions they call are included in our codebase.
In an environment where code size is so critical, this is clearly a significant
advantage.

### Quick Review

- Can you think of any pieces of code that you find yourself writing for almost
every JavaScript project? Would any of these be solved by a more complete
standard library?
- What is the advantage of working in a language that compiles to Javascript?
Can you think of any disadvantages?

## Why Clojure needs JavaScript

As useful as the Clojure language is, it needs JavaScript. The most significant
things that JavaScript enable for the Clojure language are client-side web
development, the rich ecosystem of libraries and technologies, and a much
lighter-weight platform with a smaller footprint than the Java Virtual Machine.
That said, ClojureScript compiles to JavaScript, so it runs where JavaScript
does, including the client, server, desktop, and Internet of Things (IoT)
devices.

### Client-Side Development

Clojure was originally a server-side language. It was certainly possible to
write desktop GUIs using Swing or another Java UI toolkit, but the vast majority
of Clojure was written for the server. Clojure is excellent as a server-side
programming language, but as we have discussed, it brings some significant
advantages to UI development as well. With the advent of ClojureScript, Clojure
is now a general-purpose language that can be used for almost any application -
on the server or client. As Rich Hickey stated when he announced ClojureScript,
"Clojure _rocks_, and JavaScript _reaches_."

Additionally, with technologies like Electron and NW.js, we have the option of
writing desktop applications in JavaScript as well; and since ClojureScript
compiles to JavaScript, we can take advantage of the same technologies to write
desktop applications in ClojureScript as well. While Clojure itself enables
developers to write Java GUI applications, many developers prefer the lighter-
weight style afforded by these JavaScript UI technologies.


> **ClojureScript on the Desktop**
>
> The developers of the LightTable editor - one of the most popular editors
> supporting the Clojure language - opted to build their UI using ClojureScript
> and deploy inside Electron. This enabled them to build an incredibly flexible,
> customizable UI without the complexity of a traditional desktop UI.

Finally, there are a few technologies that allow JavaScript applications to run
as mobile apps. React Native is gaining a lot of traction in this area, making
it an excellent choice for ClojureScript, since most ClojureScript UIs are built
on React as a platform. While this area of JavaScript mobile native apps is
relatively new territory, it is showing a lot of promise. The next generation of
mobile apps may be predominantly JavaScript apps, which means that ClojureScript
will be a first-class citizen for mobile clients as well.

### JavaScript Ecosystem

JavaScript is more than just a language - it is a community that has opinions on
best practices, libraries, tooling, and development process. It is in this
community that ClojureScript lives. While we as ClojureScript developers benefit
from the vast number of JavaScript libraries available, the more significant
benefit provided by JavaScript is its community. We can learn from the
collective experience of the community what is the good, the bad, and the ugly
of front-end development. The relationship between JavaScript and Clojure is
truly symbiotic, with both communities benefitting from the ideas and insights
of the other.

While we have seen that ClojureScript is a very practical and useful language,
let's face it - it is easy for a functional programming language  to lose touch
with the concerns of working programmers. Theoretical languages are useful, and
most useful programming language features started out as research projects, but
theoretical purity is not our top concern when writing web apps.
Get-it-done-ability is a much higher priority, and from its inception,
JavaScript has been about getting things done as straightforwardly as possible.
Being a citizen of the JavaScript community helps ClojureScript stay focused on
pragmatic concerns that help us build better web applications.

### Smaller Footprint

The JVM is an excellent platform for developing high-performance cross-platform
applications. It is not so excellent when it comes to running in
resource-constrained environments or scripting. While the slogan "Write once,
run anywhere" was used by Sun Microsystems to promote Java, it is ironically
JavaScript that has become a "universal" runtime. From the browser to the server
to the Raspberry Pi and embedded devices, JavaScript will run just about
anywhere. Running Java on something like a Raspberry Pi, on the other hand, is
a practical impossibility. ClojureScript is a great option for writing
applications where Java is too much bloat. Its ability to run on almost any
device is another aspect of JavaScript's "reach" that we can take advantage of
from ClojureScript.

Scripting is another area where Java is fairly weak. Whether as a scripting
language embedded in a larger application or as a system shell scripting
language, Java is too large and complex, and the startup time of the JVM makes
it impractical for short-lived programs like simple scripts. JavaScript is a
great scripting language. Node.js allows us to write system scripts as well as
web servers.

### Quick Review

- What is the most common platform for ClojureScript - web, desktop, mobile, or
IoT devices? Can it be used outside this platform?
- How well does ClojureScript interoperate with existing JavaScript tools and
libraries?

## Summary

In this lesson, we have explored the relationship of ClojureScript to its host,
JavaScript. We have learned:

- How ClojureScript improves on JavaScript's development experience
- How JavaScript's lightweight and ubiquitous runtime allows us to write
ClojureScript for practically any platform.
- Why client-side web development is a great fit for ClojureScript.

Now that we have a good understanding of both what ClojureScript is and how it
is related to the JavaScript platform, we are ready to see the language in
action. In the next section, we will work through the process of writing a
ClojureScript application, learning the common tools and practices as we go.
