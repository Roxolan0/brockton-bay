(ns brockton-bay.generation
  (:require [brockton-bay.worlds :as worlds]
            [brockton-bay.players :as players]
            [brockton-bay.util :as util]
            [brockton-bay.library :as lib]
            [brockton-bay.people :as people]
            [brockton-bay.locations :as locations]))

(defn rand-ai-players
  "Generates some random AI Players."
  [cash nb-ais]
  {:pre [(number? nb-ais)
         (<= nb-ais (count lib/ai-names))]}
  (map #(players/->Player
         %
         false
         cash)
       (util/rand-no-repeat nb-ais lib/ai-names)))

(defn human-players
  "Generates a human Player for each name."
  [cash names]
  (map #(players/->Player
         %
         true
         cash)
       names))

(defn rand-person
  "Picks a random stat template from library and generates a Person with it and the player-id."
  [player-id]
  (let [template (rand-nth (seq lib/people-templates))]
    (people/->Person
      (key template)
      (val template)
      player-id
      nil)))

(defn add-rand-people
  "Adds a randomly-generated Person with the player-id to the world."
  ([world player-id]
   {:pre [(worlds/world? world)]}
   (util/add-with-id world [:people] (rand-person player-id)))
  ([world nb-templates player-id]
   {:pre [(worlds/world? world)]}
   (reduce add-rand-people world (repeat nb-templates player-id))))

(defn add-rand-people-to-everyone
  "Adds an equal number of randomly-generated People to all players."
  [world nb-templates]
  {:pre [(worlds/world? world)]}
  (reduce
    (fn [a-world player-id] (add-rand-people a-world nb-templates player-id))
    world
    (keys (:players world))))

(defn rand-locations
  "Generates some random Locations."
  [nb-locations]
  {:pre [(number? nb-locations)
         (<= nb-locations (count lib/location-names))]}
  (map #(locations/->Location
         %
         0
         {})
       (util/rand-no-repeat nb-locations lib/location-names)))

(defn generate [human-names nb-ais]
  {:pre [(number? nb-ais)]}
  (let [nb-players (+ nb-ais (count human-names))]
    (-> worlds/empty-world
        (util/add-many-with-id [:players] (human-players lib/starting-cash human-names))
        (util/add-many-with-id [:players]  (rand-ai-players lib/starting-cash nb-ais))
        (add-rand-people-to-everyone lib/people-per-player)
        (util/add-many-with-id [:locations]  (rand-locations (lib/nb-locations nb-players))))))