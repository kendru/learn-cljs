(ns chat.components.auth
  (:require [goog.dom :as gdom]
            [goog.dom.classes :as gdom-classes]
            [chat.components.component :refer [init-component]]
            [chat.components.helpers :as helpers]
            [chat.command :as cmd]
            [chat.state :as state])
  (:import [goog.dom TagName]))

(defn accessor [state]
  (select-keys state [:current-user :auth-modal]))

(defn auth-modal [state cmd-ch {:keys [header-text
                                       form-fields
                                       submit-action
                                       footer-text]}]
  (gdom/createDom TagName.DIV "auth-modal-inner"
    (gdom/createDom TagName.DIV "auth-modal-header"
      (gdom/createDom TagName.H1 nil header-text))
    (gdom/createDom TagName.DIV "auth-modal-body"
      (doto
        (gdom/createDom TagName.FORM nil
          (apply gdom/createDom TagName.DIV nil
            (for [{:keys [label type name]} form-fields
                  :let [id (str "auth-field-" name)]]
              (gdom/createDom TagName.DIV "input-field"
                (gdom/createDom TagName.LABEL #js {"class" "input-label"
                                                   "for" id}
                  label)
                (gdom/createDom TagName.INPUT #js {"type" type
                                                   "name" name
                                                   "id" id}))))
          (gdom/createDom TagName.BUTTON #js {"type" "submit"} "Submit"))
        (.addEventListener "submit"
          #(do
             (.preventDefault %)
             (cmd/dispatch! cmd-ch submit-action
               (into {}
                 (for [{:keys [name]} form-fields
                       :let [id (str "auth-field-" name)]]
                   [(keyword name) (.-value (gdom/getElement id))])))))))
    (gdom/createDom TagName.DIV "auth-modal-footer"
      (doto (gdom/createDom TagName.A nil footer-text)
        (.addEventListener "click"
          #(do (.preventDefault %)
               (cmd/dispatch! cmd-ch :toggle-auth-modal nil)))))))

(defn sign-in-modal [state cmd-ch]
  (auth-modal state cmd-ch
    {:header-text "Sign In"
     :footer-text "New here? Sign up."
     :form-fields [{:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-in}))


(defn sign-up-modal [state cmd-ch]
  (auth-modal state cmd-ch
    {:header-text "Sign Up"
     :footer-text "Already have an account? Sign in."
     :form-fields [{:label "First Name" :type "text" :name "first-name"}
                   {:label "Last Name" :type "text" :name "last-name"}
                   {:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-up}))

(defn get-render [cmd-ch]
  (fn [el {:keys [current-user auth-modal] :as s}]
    (if (some? current-user)
      (gdom-classes/add el "hidden")
      (doto el
        (gdom-classes/remove "hidden")
        (.appendChild
          (gdom/createDom TagName.DIV "auth-modal-wrapper"
            (if (= :sign-in auth-modal)
              (sign-in-modal s cmd-ch)
              (sign-up-modal s cmd-ch))))))))

(defn init-auth [cmd-ch]
  (cmd/intercept!
    ;; For any messages while the user is logged out (other than messages relating to auth)
    (fn [msg]
      (and (nil? (accessor @state/app-state))
           (nil? (#{:sign-in :sign-up :toggle-auth-modal :api/authenticated :api/authentication-error} (:chat.command/type msg)))))
    ;; Drop the message
    (constantly nil))

  (doto (gdom/createDom TagName.SECTION "auth-modal")
    (init-component :auth accessor (get-render cmd-ch))))

