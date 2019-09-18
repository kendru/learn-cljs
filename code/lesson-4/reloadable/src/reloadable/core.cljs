(ns reloadable.core)

(defn append-paragraph [text]
  (let [el (.createElement js/document "p")]
  (aset el "textContent" text)
  (.appendChild (.-body js/document)
                el)))

(append-paragraph "Reload me!")

