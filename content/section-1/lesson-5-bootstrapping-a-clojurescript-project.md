---
title: "Bootstrapping a Clojurescript Project | Lesson 5"
type: "docs"
date: 2019-09-19T20:45:29-06:00
---

# Lesson 5: Bootstrapping a Clojurescript Project

Until this point, our discussion of ClojureScript has been largely
theoretical. We have an idea of why we would want to use ClojureScript, but what
does it look like in action? Over the course of this unit, we'll develop a
small weather forecasting application from scratch. We will pay attention to the
high-level concepts while leaving the discussion of the particulars until later.
At this point we are interested in getting used to the look of ClojureScript
code, identifying how it makes things that are difficult in JavaScript easier,
and how the tooling helps streamline the development process. Although the
syntax of the application may still seem a bit foreign, we'll start to get a
feel for how fun and productive a ClojureScript project can be.

---

*In this lesson:*

- Walk through setting up a project from scratch
- Learn how to use Leiningen, the leading ClojureScript build tool
- Explore the structure of a fresh project

---

To start out, we will learn how to create and build a ClojureScript project.
Just as the carpenter must be familiar with all of his tools before he can
create a masterpiece, we must get acquainted to the tools of our trade. Coming
from the glut of tools that we have need for in JavaScript development, it should
come as a relief that there are only a few key tools that we need for any
ClojureScript project.

## Meeting Leiningen

Each language has its own set of tools to learn, and ClojureScript is no
different. In this book, we will focus on two very important tools - Leiningen
for general-purpose build tasks, and Figwheel for live reloading of code. We'll
begin by looking at our new friend Leiningen, the build tool with the funny
name. Much like JavaScript's Grunt, Leiningen is a configuration-based build
tool and task runner. The reader who has some familiarity with building software
projects should feel at home rather quickly, but do not worry if this is your
first exposure to using a build-process. We will walk through the essentials in
enough detail to get comfortable building ClojureScript applications.

Leiningen is the defacto build tool for Clojure and ClojureScript. We will use
it for bootstrapping a project, managing dependencies, compiling, and testing
our projects. Using a single tool for all of these concerns should come as a
welcome change from the proliferation of tools in the JavaScript
landscape. Before proceeding any further, we should install Java and
Leiningen. See Appendix A for details on installing these tools for your
platform. One interesting feature of ClojureScript, is that we do not need to
install it manually - we only need to specify it as a dependency of our
project. Once we have Leiningen installed, bootstrapping a new project is as
simple as running a single command:

#### Using Leiningen to bootstrap a new project

```
$ lein new my-cljs-project
```

Leiningen comes with a large command-line interface that provides access to
common tasks such as creating a new project or running tests. Simply running the
command, `lein`, with no arguments will list all of the commands available. For
more help on a specific task (such as `new` in the command we just ran), we can
run `lein help TASK` where `TASK` is the name of any Leiningen task available.
While Leiningen provides a number of tasks by default, we will often run tasks
provided by plugins.

NOTE: The terminal examples in this book are for a Unix-like environment such as
OSX or Linux. Windows users may have to make minor adaptations the commands.

### Understanding Leiningen

Similar to JavaScript tools such as Grunt and Gulp, Leiningen provides a
platform for creating build-related and utility tasks. However, the focus of
Leiningen is broader than that of its JavaScript counterparts. For instance,
Leiningen can scaffold [Scaffolding is the process of generating the files and
directories that will be necessary for most projects. Without scaffolding, we
would have to manually create the same files by hand for every new project.] a
new project, manage dependencies, run build tasks, and deploy a completed
application. In the JavaScript world, one might use four separate tools to cover
each of these concerns: Yeoman for scaffolding, NPM for managing dependencies,
Grunt for running build tasks, and ad-hoc bash scripts for deploying. Compared
to the overhead of learning four tools to manage a JavaScript app, using a single,
well-supported tool for a ClojureScript application is a simpler alternative.

In the examples in this book, we will only be scratching the surface of
Leiningen's capabilities. It is quite a capable tool for managing both Clojure
and ClojureScript projects, and it has very good documentation and examples on
its website. Below is a summary of the only tasks that we will use in this
book. As we can see, it will not be too difficult to remember how to use these
tasks.

| Name | Usage | Description |
| ---- | ----- | ----------- |
| new | `lein new project-name` | Creates a new project |
| figwheel | `lein figwheel dev` | Automatically recompiles code and pushes live updates to a web browser |
| doo | `lein doo chrome test` | Runs ClojureScript tests in any number of browsers |
| cljsbuild | `lein cljsbuild once` | Compiles ClojureScript to JavaScript for deployment |

_Summary of key Leiningen Tasks_

### Quick Review

- Create a new Leiningen project called `cljs-here-i-come`

## Creating a Project

Now that we have had a whirlwind tour of Leiningen, let's dive in and create our
first simple project, a weather forecasting app. Since we will be using Figwheel
to automatically compile and (re-)load[?] code in the browser as we make changes, we can make
use of a Leiningen _template_, which is a blueprint for the files and directory
structure to create. By default there are several built-in templates for
generating Clojure applications and Leiningen projects, but we can specify other
templates as well. When invoking `lein new` with a template name, Leiningen will
check to see if the template is a built-in one or one that is available locally.
If it cannot find a built-in or local template, it will try to find the
appropriate template from a central repository, download it, and generate our
project.

![Leiningen Template Resolution](/img/lesson5/lein-template-resolution.png)

_Leiningen Template Resolution_

The Figwheel project provides a template that generates a ClojureScript project
with all the plumbing required for live reloading. We will be using the Reagent
library for building the UI, and thankfully the Figwheel template allows us to
pass an additional option to include Reagent boilerplate code in the generated
project. We can now create the project for our app.

```
$ lein new figwheel cljs-weather -- --reagent
```

Since this command includes some unfamiliar syntax, let's take a moment to
dissect it. As we just learned, the first part of the command, `lein new
figwheel`, creates a new project using the `figwheel` template. Since `figwheel`
is not a built-in template, and we probably have not created a template by that
name locally, Leiningen will fetch the template from Clojure's central
repository, [Clojars](https://clojars.org/). The remaining portion of
the command is passed to the template. By convention, the next argument
("cljs-weather" in our case) is the name of the project and is used to
determine the name of the project's directory and top-level namespace. Templates
also commonly use the project name in a `README` or other generated files. There
is no convention for the remaining arguments, but templates commonly allow users
to supply a number of additional flags. In the documentation for the [Figwheel template](https://github.com/bhauman/figwheel-template), the author
indicates that for clarity's sake, we should separate template-specific options
from the rest of the command by a `--`. The final argument that we pass in is
a flag, `--reagent`, indicating that we want the template to generate code for
the Reagent framework. We'll be using both Figwheel and Reagent extensively
through the course of this book.

![Dissecting a lein new Command](/img/lesson5/new_new_dissected.png)

_Dissecting a `lein new` Command_


## Exploring the Project

We now have a running (albeit skeletal) ClojureScript project. To see the
application that Leiningen generated, we can navigate into the project directory
and see what files were generated.

```
$ cd cljs-weather
$ tree -a                    <1>
.
├── .gitignore
├── README.md
├── project.clj
├── resources                <2>
│   └── public
│       ├── css
│       │   └── style.css
│       └── index.html
└── src                      <3>
    └── cljs_weather
        └── core.cljs
```

_Exploring the Generated Project_

1. View the contents of the project directory recursively (Windows users may try `tree /f`)
2. The `resources` directory contains the HTML page that will load our application as well as any styles and assets we need.
3. The `src` directory contains ClojureScript sourc code

Leiningen generated several top-level files, a `src` directory, and a `resources`
directory. Leiningen uses the `project.clj` file for all configuration that it
needs, including project metadata (name, version, etc.), dependencies, and
ClojureScript compiler options. This file is the equivalent of `package.json` in
a JavaScript project that uses NPM. We will be digging into this file as we
build more applications in Section 3. For now, we only need to know how it is
used. Finally, the .gitignore file will exclude all of the local files that
Leiningen, the ClojureScript compiler, or figwheel might generate. All things
considered, this is quite a bit of boilerplace that was handled by a single
command.

The `src` directory contains all of the ClojureScript source files for our
project. Usually, there will be a single folder under `src` that shares the same
name as our project, and under this folder, there can be any number of `*.cljs`
files and other folders. If we open `core.cljs` in a text editor or IDE that
supports ClojureScript [^1], we will see something like this:

![Editing core.cljs with Emacs](/img/lesson5/emacs_screenshot.png)

_Editing `core.cljs` with Emacs_

We will dig in to the rest of this file over the next couple of lessons, as we
start to build out the weather forecasting app. For now, we will look at the
namespace declaration at the top of the file, since it is closely tied to the
structure of the project. Each ClojureScript file contains a single _namespace_,
which is simply a collection of data and functions. The namespace is the unit of
modularity in ClojureScript. We can see the namespace declared on the first line: `(ns
cljs-weather.core ...)`. The ClojureScript compiler uses a simple naming
convention for namespaces based on the name of the file that houses them:

1. Take the file path relative to the source directory
2. Replace the path separator ("/" on Unix-like systems and "\" on Windows) with a dot, "."
3. Replace underscores "_", with hyphens, "-"

![Filename to Namespace Convention](/img/lesson5/namespace-transformation.png)

_Filename to Namespace Convention_

> *Hyphen or Underscore?*
>
> One detail that sometimes trips up newcomers to ClojureScript is the fact that
> we name directories in the project with underscores but we name namespaces with
> hyphens. This is a convention borrowed from Clojure, which compiles namespaces
> into Java classes, naming the classes according to their file path. Since
> hyphens are not allowed in Java class names, they are not allowed in the file
> paths either. ClojureScript follows Clojure's lead and requires that hyphens in
> a namespace be converted to underscores in the filesystem path. It is a quirk,
> but it is one that is easy to live with once we are aware of it.

The `resources/` directory contains all of the assets that we need to serve a
website, including an `index.html`, a stylesheet (which is empty by default),
and once we build our project, it will additionally contain all of the compiled
JavaScript code as well. The template created the `index.html` with a
single div that we can load our application into and includes the JavaScript
file that will load our application as well as all of its dependencies. This is
fine for development, but when it comes time to deploy, we probably want to
compile all of the modules from our code and everything that it depends on into
a single file so that we can compress it more effectively and transfer it in a
single request.

### Quick Review

- What file would you change to tweak the markup of the page that will load your app?
- What file would you change to add project dependencies?
- What file would you create to add a `cljs-weather.sunny-day` namespace?

We already get a taste on ClojureScript's focus on simplicity: bootstrapping a
new project did not create dozens of files full of boilerplate code; it only
created 4 project-related files, plus a `.gitignore` and a README. The amazing
thing is that we really do not need more than this to start a new project.
ClojureScript development focuses on building incrementally from a small base!

### Challenge

Visit Leiningen's website and explore what you can do with it. Remember that
Leiningen is a build tool for both ClojureScript and Clojure (JVM), so some of
the instruction is geared towards Clojure. Here are a few exercises to help
understand what Leiningen does for you:

- Create a new leiningen project from the _mies_ template and see what (if anything) is different from the project that we generated.
- Replicate the files that Leiningen generated by hand. Was it difficult?
- Write out the name of each of the files that was generated and explain the purpose of each.

## Summary

In this lesson, we have walked through the process of creating a new
ClojureScript project from scratch. We were introduced to Leiningen, the most
widely-used ClojureScript build tool, and we explored the project structure that
it generated for us. Next, we will learn about Figwheel, the other core tool
that will enable us to receive immediate feedback while we are developing. After
that, we will be able to jump in with both feet and start writing code. We now
know:

- How to set up a brand new ClojureScript project using Leiningen
- What sort of tasks are handled by the Leiningen build tool
- How a typical ClojureScript project is laid out

[^1]: Most programmers' text editors have a Clojure language package that can be used for ClojureScript, but if you prefer working with IDEs, Cursive is by far the most fully featured Clojure(Script) IDE available.
