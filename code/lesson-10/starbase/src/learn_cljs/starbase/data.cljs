(ns learn-cljs.starbase.data)

(def game {:start {:type :start
                   :title "Starbase Lambda"
                   :dialog (str "Welcome, bold adventurer! You are about to embark on a dangerous "
                                "quest to find the Tetryon Singularity.\nAre you up to the task?")
                   :transitions {"yes" :embarked
                                 "no" :coward}}
           :coward {:type :lose
                    :title "Get lost, space scum!"
                    :dialog (str "What a coward! You didn't even give it a shot. You deserve to stay "
                                 "home while the true heroes go for glory!")}
           :embarked {:type :continue
                      :title "Transported to Trenoble Expanse"
                      :dialog (str "No sooner did you leave the starbase than you were chased our of "
                                   "your own star system by a gang of space pirates. You find yourself "
                                   "in the middle of the Trenoble expanse. Going back to base would be "
                                   "the safe bet, but if you continue, glory awaits! ...Or you die. "
                                   "Continue?")
                      :transitions {"yes" :neighboring-system
                                    "no" :coward}}
           :neighboring-system {:type :continue
                                :title "In the neighboring star system"
                                :dialog (str "After weeks of long, hard travel, you arrive in the "
                                             "neighboring system. The natives are friendly, and with "
                                             "the exception of a slight cranial ridge, they look just "
                                             "like humans. You are in dire need of fuel, and a suspicious "
                                             "merchant approaches and offers to repair your ship only if "
                                             "you give him all of your reserves of alcohol. Do you make "
                                             "the deal?")
                                :transitions {"yes" :into-the-nebula
                                              "no" :stranded}}
           :stranded {:type :continue
                      :title "Stranded on a strange planet"
                      :dialog (str "Come to find out, it takes fuel to fly a starship. Who knew? Now you "
                                   "are stranded on this strange planet with only your wits and a really "
                                   "cool (but out of comission) star ship. You need to make a buck, and "
                                   "you see a sign in a restaurant window that reads, \"Chef Wanted\" "
                                   "(yes, in English). Do you apply for the job?")
                      :transitions {"yes" :off-the-planet
                                    "no" :retire-on-panet}}
           :retire-on-panet {:type :lose
                             :title "Retire on the planet"
                             :dialog (str "After weeks of looking for a quick way to leave the planet, "
                                          "you grow discouraged and disollusioned with the quest for the "
                                          "Tetryon Singularity, and you decide to settle down where you "
                                          "are and make a new life for yourself. In fact, you eventually "
                                          "retire here. While you may tell yourself that you're happy "
                                          "here, we all know that you're really just a quitter.")}
           :off-the-planet {:type :skip
                            :title "Back in Space"
                            :dialog (str "After a few months of hard work, you have earned enough "
                                         "of the local currency to refuel. While it is disappointing "
                                         "to have had such a set-back, it is good to be back in "
                                         "space.")
                            :on-continue :into-the-nebula}
           :into-the-nebula {:type :continue
                             :title "Into the Nebula"
                             :dialog (str "What good is a quest in space without a nebula? The "
                                          "storms! The unknown territory! The almost near certain "
                                          "prospect of death! This is the stuff adventures are made "
                                          "of. Do you dare continue?")
                             :transitions {"yes" :discovery
                                           "no" :die-in-nebula}}
           :die-in-nebula {:type :lose
                           :title "Oh Snap. You Died."
                           :dialog (str "Unfortunately, as soon as you started back for home, your "
                                        "hyperdrive agitation manifold malfunctioned, sucking up all "
                                        "your breathable atmosphere, and that was that. Better luck "
                                        "next time! ...Wait, there is no next time.")}
           :discovery {:type :win
                       :title "An Unexpected Discovery"
                       :dialog (str "No sooner did you enter the nebula than you came across a "
                                    "singularity. Moreover, it was emitting large quantities of "
                                    "tetryon particles! It looks like your quest is over, space "
                                    "person. Were you expecting more? Sorry - this is it.")}})
