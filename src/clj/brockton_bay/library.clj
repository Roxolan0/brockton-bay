(ns brockton-bay.library)

;;; Game constants

(def people-per-player 6)
(def starting-cash 0)
(def nb-turns 3)
(def nb-fight-rounds 5)
(def cash-taken-by-fleeing-people 20)
(def betrayal-damage 2)

(defn nb-locations [nb-players] (+ 1 nb-players))


;;; Game defrecords (here to avoid circular dependencies)
;;; HACK: this excuse doesn't sound right.

(defrecord Person-stats
  [^long speed
   ^long damage
   ^long armour
   ^long hp])

;;; Predefined game elements

(def location-names
  ["Drug trade"
   "Bank robbery"
   "Mercenary work"
   "Kidnapping"
   "Train job"
   "Honest work"
   "Crystal harvest"
   "Prison break"
   "Bodyguarding"
   "Black market"
   "Close-range hacking"
   "Publicity stunt"
   "Assassination"])

(def ai-names
  ["The Azian Bad Boys"
   "The Pure"
   "Fenrir's Chosen"
   "The Merchants"
   "The Travelers"
   "The Undersiders"
   "The Protectorate"
   "New Wave"
   "The Ambassadors"
   "The Fallen"
   "The Teeth"
   "The Adepts"])

(def people-templates
  (zipmap
    ["Average Lad"
     "Lightning Bruiser"
     "Glass Cannon"
     "Runner"
     "Tortoise"
     "Tank"
     "Beater"
     "Survivor"]
    (map (partial apply ->Person-stats)
         ['(5 3 1 5)                                        ;Average Lad
          '(8 4 0 4)                                        ;Lightning Bruiser
          '(0 5 0 8)                                        ;Glass Cannon
          '(10 1 1 5)                                       ;Runner
          '(4 3 3 3)                                        ;Tortoise
          '(1 2 2 8)                                        ;Tank
          '(3 4 2 7)                                        ;Beater
          '(6 2 1 8)                                        ;Survivor
          ])))
