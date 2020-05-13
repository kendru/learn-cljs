(ns chat.components.component
  (:require [chat.state :as state]))

(defn init-component
  "Initialize a component.
  Parameters:
  el - Element in which to render component
  watch-key - Key that uniquely identifies this component
  accessor - Function that takes the app state and returns the component state
  render - Function that takes the parent element and component state and renders DOM"
  [el watch-key accessor render]
  (add-watch state/app-state watch-key
    (fn [_ _ old new]
      (let [state-old (accessor old)
            state-new (accessor new)]
        (when (not= state-old state-new)
          (set! (.-innerText el) "")
          (render el state-new)))))
  (render el (accessor @state/app-state))
  el)
