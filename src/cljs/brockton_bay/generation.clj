(ns brockton-bay.generation
  (:require [brockton-bay.worlds :as worlds]
            [brockton-bay.players :as players]
            [brockton-bay.util :as util]
            [brockton-bay.library :as lib]
            [brockton-bay.people :as people]))

(defn add-ai-players
  [world cash nb-ais]
  {:pre [(worlds/world? world)
         (number? nb-ais)]}
  (let [names (take nb-ais (shuffle lib/ai-names))]
    (reduce #(util/add-with-id %1 :players (players/->Player %2 false cash))
            world
            names)))

(defn add-templates
  ;; TODO: split in half
  "Pick a random stat template from library and generate a Person with the player-id from it."
  ([world player-id]
   {:pre [(worlds/world? world)]}
   (let [template (rand-nth (seq lib/people-templates))]
     (util/add-with-id world :people (people/->Person
                                       (key template)
                                       (val template)
                                       player-id
                                       nil))))
  ([world nb-templates player-id]
   {:pre [(worlds/world? world)]}
   (reduce add-templates world (repeat nb-templates player-id))
    ))

(defn add-templates-to-everyone
  [world nb-templates]
  {:pre [(worlds/world? world)]}
  (reduce
    #(add-templates %1 nb-templates %2)
    world (keys (:players world))))

(defn generate [human-names nb-ais]
  {:pre [(number? nb-ais)]}
  (as->
    worlds/empty-world $
    (reduce
      #(util/add-with-id %1 :players (players/->Player %2 true lib/starting-cash))
      $
      human-names)
    (add-ai-players $ lib/starting-cash nb-ais)
    (add-templates-to-everyone $ lib/people-per-player)
    (worlds/add-locations $ lib/nb-locations)
    ))