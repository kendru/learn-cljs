

We can extend our mental model to accommodate the concept of a closure by
thinking of a function as the combination of the function's body and an `environment`,
which is a table of all of the symbols that were visible when it was defined, including its
formal parameters. When evaluating the function,

// TODO: Diagram illustrating environment lookup