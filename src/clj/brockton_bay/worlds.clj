(ns brockton-bay.worlds
  (:require [brockton-bay.util :as util]
            [brockton-bay.library :as lib]
            [brockton-bay.locations :as locations]))

(defrecord World
  [players
   locations
   people
   turn-count])

(defn world? [x] (instance? World x))

(def empty-world
  (->World {} {} {} 0))

(defn agreement? [world location-id player1-id player2-id]
  {:pre [(world? world)]}
  (->>
    (get-in world [:locations location-id :agreements])
    (vals)
    (map :choices-by-player-id)
    (filter #(util/contains-many? % player1-id player2-id))
    (empty?)
    (not)))

(defn is-at? [world location-id person-id]
  {:pre [(world? world)]}
  (= location-id (get-in world [:people person-id :location-id])))

(defn higher-speed? [world person1-id person2-id]
  {:pre [(world? world)]}
  (>
    (get-in world [:people person1-id :stats :speed])
    (get-in world [:people person2-id :stats :speed])))

(defn fleeing?
  ; HACK: should use a function that works for any combination of agreements.
  [world person-id]
  {:pre [(world? world)
         (contains? (:people world) person-id)]}
  (let [player-id (get-in world [:people person-id :player-id])
        location-id (get-in world [:people person-id :location-id])]
    (as-> world $
          (get-in $ [:locations location-id :agreements])
          (vals $)
          (map :choices-by-player-id $)
          (map #(get % player-id) $)
          (some #(= :flee %) $)
          (some? $))))                                      ; HACK : something's up with these two 'some'

(defn sharing?
  ; HACK: should use a function that works for any combination of agreements.
  [world person1-id person2-id]
  {:pre [(world? world)
         (= (get-in world [:people person1-id :location-id])
            (get-in world [:people person2-id :location-id]))]}
  (let [player1-id (get-in world [:people person1-id :player-id])
        player2-id (get-in world [:people person2-id :player-id])
        location-id (get-in world [:people person1-id :location-id])]
    (as-> world $
          (get-in $ [:locations location-id :agreements])
          (vals $)
          (map :choices-by-player-id $)
          (filter #(util/contains-many? % player1-id player2-id) $)
          (filter #(= :share (get % player1-id)) $)
          (filter #(= :share (get % player2-id)) $)
          (not (empty? $)))))

(defn betraying?
  ; HACK: should use a function that works for any combination of agreements.
  [world betrayer-id victim-id]
  {:pre [(world? world)
         (= (get-in world [:people betrayer-id :location-id])
            (get-in world [:people victim-id :location-id]))]}
  (let [player-betrayer-id (get-in world [:people betrayer-id :player-id])
        player-victim-id (get-in world [:people victim-id :player-id])
        location-id (get-in world [:people betrayer-id :location-id])]
    (as-> world $
          (get-in $ [:locations location-id :agreements])
          (vals $)
          (map :choices-by-player-id $)
          (filter #(util/contains-many? % player-betrayer-id player-victim-id) $)
          (filter #(= :attack (get % player-betrayer-id)) $)
          (filter #(= :share (get % player-victim-id)) $)
          (not (empty? $)))))

(defn by-speed-decr [world]
  {:pre [(world? world)]}
  (comparator (partial higher-speed? world)))

(defn get-players-cash [world]
  {:pre [(world? world)]}
  (zipmap
    (map :name (vals (:players world)))
    (map :cash (vals (:players world))))
  )

(defn get-people-at [world location-id]
  {:pre [(world? world)]}
  (filter
    #(= location-id (:location-id (val %)))
    (:people world)))

(defn get-people-without-location [world]
  {:pre [(world? world)]}
  (get-people-at world nil))

(defn get-dying-people-ids [world]
  {:pre [(world? world)]}
  (keys (filter
          #(>= 0 (get-in (val %) [:stats :hp]))
          (:people world))))

(defn get-players-ids-at [world location-id]
  {:pre [(world? world)]}
  (->>
    (get-people-at world location-id)
    (vals)
    (map :player-id)
    (distinct)))

(defn get-local-enemies-ids
  ; HACK: can be done cleaner, and using get-people-ids
  [world person-id]
  {:pre [(world? world)
         (contains? (:people world) person-id)]}
  (let [person (get-in world [:people person-id])]
    (as->
      world $
      (:people $)
      (filter
        #(= (:location-id person) (:location-id (val %))) $)
      (filter
        #(not= (:player-id person) (:player-id (val %))) $)
      (filter
        #(not (sharing? world person-id (key %))) $)
      (keys $))))

(defn get-people-ids
  ([world]
   {:pre [(world? world)]}
   (-> world
        (:people)
        (keys)))
  ([world location-id]
   {:pre [(world? world)]}
   (filter
     (partial is-at? world location-id)
     (get-people-ids world))))

(defn get-people-ids-by-speed
  ([world]
   {:pre [(world? world)]}
   (sort
     (by-speed-decr world)
     (get-people-ids world)))
  ([world location-id]
   {:pre [(world? world)]}
   (sort
     (by-speed-decr world)
     (get-people-ids world location-id))))

(defn get-betrayal-damage [world attacker-id target-id]
  {:pre [(world? world)]}
  (if (betraying? world attacker-id target-id)
    lib/betrayal-damage
    0))
