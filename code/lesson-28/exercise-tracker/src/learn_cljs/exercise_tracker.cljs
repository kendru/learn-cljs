(ns learn-cljs.exercise-tracker
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.ratom :as ratom]
            [goog.dom :as gdom]))

(defn- date-string [d]
  (let [pad-zero #(.padStart (.toString %) 2 "0")
        y (.getFullYear d)
        m (-> (.getMonth d) inc pad-zero)
        d (pad-zero (.getDate d))]
    (str y "-" m "-" d)))

(defn initial-inputs []
  {:date (date-string (js/Date.))
   :minutes "0"})

(defonce state
  (r/atom {:inputs (initial-inputs)
           :entries {}}))

(def chart-width 400)
(def chart-height 200)
(def bar-spacing 2)

(defn get-points [entries]
  (let [ms-in-day 86400000
        chart-days 30
        now (js/Date.now)]
    (map (fn [i]
           (let [days-ago (- chart-days (inc i))
                 date (date-string (js/Date. (- now (* ms-in-day days-ago))))]
             (get entries date 0)))
         (range chart-days))))

(defn chart []
  (let [entries (r/cursor state [:entries])
        chart-data (ratom/make-reaction
                    #(let [points (get-points @entries)]
                       {:points points
                        :chart-max (reduce max 1 points)}))]
    (fn []
      (let [{:keys [points chart-max]} @chart-data
            bar-width (- (/ chart-width (count points))
                         bar-spacing)]
        [:svg.chart {:x 0 :y 0
                     :width chart-width :height chart-height}
         (for [[i point] (map-indexed vector points)
               :let [x (* i (+ bar-width bar-spacing))
                     pct (- 1 (/ point chart-max))
                     bar-height (- chart-height (* chart-height pct))
                     y (- chart-height bar-height)]]
           [:rect {:key i
                   :x x :y y
                   :width bar-width
                   :height bar-height}])]))))

(defn date-input []
  (let [val (r/cursor state [:inputs :date])]
    (fn []
      [:div.input-wrapper
       [:label "Day"]
       [:input {:type "date"
                :value @val
                :on-change #(reset! val (.. % -target -value))}]])))

(defn time-input []
  (let [val (r/cursor state [:inputs :minutes])]
    (fn []
      [:div.input-wrapper
       [:label "Time (minutes)"]
       [:input {:type "number" :min 0 :step 1
                :value @val
                :on-change #(reset! val (.. % -target -value))}]])))

(defn submit-button []
  [:div.actions
   [:button {:type "submit"} "Submit"]])

(defn submit-form [state]
  (let [{:keys [date minutes]} (:inputs state)]
    (-> state
        (assoc-in [:entries date] (js/parseInt minutes))
        (assoc :inputs (initial-inputs)))))

(defn form []
  [:form.input-form {:on-submit (fn [e]
                                  (.preventDefault e)
                                  (swap! state submit-form))}
   [date-input]
   [time-input]
   [submit-button]])

(defn app []
  [:div.app
   [chart]
   [form]])

(rdom/render
 [app]
 (gdom/getElement "app"))
