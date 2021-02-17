(ns learn-cljs.chat.components.auth
  (:require [learn-cljs.chat.components.dom :as dom]
            [goog.dom.classes :as gdom-classes]
            [learn-cljs.chat.components.component :refer [init-component]]
            [learn-cljs.chat.message-bus :as bus]))

(declare accessor get-render sign-in-modal sign-up-modal
         auth-modal auth-form footer-link)

(defn init-auth [msg-ch]
  (init-component (dom/section "auth-modal")
    :auth
    accessor
    (get-render msg-ch)))

(defn accessor [state]
  (select-keys state [:current-user :auth-modal]))

(defn get-render [msg-ch]
  (fn [el {:keys [current-user auth-modal] :as s}]
    (if (some? current-user)
      (gdom-classes/add el "hidden")
      (doto el
        (gdom-classes/remove "hidden")
        (.appendChild
          (dom/div "auth-modal-wrapper"
            (if (= :sign-in auth-modal)
              (sign-in-modal msg-ch)
              (sign-up-modal msg-ch))))))))

(defn sign-in-modal [msg-ch]
  (auth-modal msg-ch
    {:header-text "Sign In"
     :footer-text "New here? Sign up."
     :form-fields [{:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-in}))

(defn sign-up-modal [msg-ch]
  (auth-modal msg-ch
    {:header-text "Sign Up"
     :footer-text "Already have an account? Sign in."
     :form-fields [{:label "First Name" :type "text" :name "first-name"}
                   {:label "Last Name" :type "text" :name "last-name"}
                   {:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-up}))

(defn auth-modal [msg-ch {:keys [header-text
                                 form-fields
                                 submit-action
                                 footer-text]}]
    (dom/div "auth-modal-inner"
      (dom/div "auth-modal-header"
        (dom/h1 nil header-text))
      (dom/div "auth-modal-body"
        (auth-form msg-ch form-fields submit-action))
      (dom/div "auth-modal-footer"
        (footer-link msg-ch footer-text))))

(defn auth-form [msg-ch form-fields submit-action]
  (let [form (dom/form nil
               (apply dom/with-children (dom/div)
                 (for [{:keys [label type name]} form-fields
                       :let [id (str "auth-field-" name)]]
                   (dom/div "input-field"
                     (dom/label #js {"class" "input-label"
                                     "for" id}
                       label)
                     (dom/input #js {"type" type
                                     "name" name
                                     "id" id}))))
               (dom/button #js {"type" "submit"} "Submit"))]
    (doto form
      (.addEventListener "submit"
        (fn [e]
          (.preventDefault e)
          (bus/dispatch! msg-ch submit-action
            (into {}
              (for [{:keys [name]} form-fields
                    :let [id (str "auth-field-" name)]]
                [(keyword name) (.-value (js/document.getElementById id))]))))))))

(defn footer-link [msg-ch footer-text]
  (doto (dom/a nil footer-text)
    (.addEventListener "click"
      (fn [e]
        (.preventDefault e)
        (bus/dispatch! msg-ch :toggle-auth-modal)))))

