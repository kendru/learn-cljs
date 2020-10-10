(ns estimates.ui.animation
  (:require [reagent.core :as r]))

(defn duration [ms]
  (into [:span {:style {"--anim-duration" (str ms "ms")}}]
        (r/children (r/current-component))))

(defn slide-in [opts]
  (let [{:keys [direction speed]
         :or {direction :left}} opts
        triggered? (r/atom false)]
    (js/requestAnimationFrame #(reset! triggered? true))
    (fn [_ _]
      (into
        [:div {:class (str "slide-in"
                          " transition-" (if @triggered? "triggered" "initial")
                          " direction-" (name direction))}]
        (r/children (r/current-component))))))

