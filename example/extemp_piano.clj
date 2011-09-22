(ns example.extemp-piano
  (:use [overtone.live]
        [overtone.inst piano synth drum]))

;; This example has been translated from the Extempore code demonstrated in
;; http://vimeo.com/21956071 (found around the 10 minute mark)

;; Original Extempore code:
;; (load-sampler sampler "/home/andrew/Documents/samples/piano")
;; (define scale (pc:scale 0 'aeolian))
;; (define loop
;;   (lambda (beat dur root)
;;      (for-each (lambda (p offset)
;;                   (play (+ offset) sampler p 100 (* 2.0 dur)))
;;                (pc:make-chord 40 (cosr 75 10 1/32) 5
;;                               (pc:chord root (if (member root '(10 8))
;;                                                '^7
;;                                                '-7)))
;;                '(1/3 1 3/2 1 2 3))
;;      (callback (*metro* (+ beat (* 0.5 dur))) 'loop (+ dur beat)
;;                dur
;;                (if (member root '(0 8))
;;                  (random '(2 7 10))
;;                  (random '(0 8))))))

(def instrument piano)
(def metro (metronome 20))

(def beat-offsets [0 0.1 1/3  0.7 0.9])
(def chord-prog
  [#{[2 :minor7] [7 :minor7] [10 :major7]}
   #{[0 :minor7] [8 :major7]}])
(def root 40)
(def max-range 35)
(def range-variation 10)
(def range-period 8)

(defn play-with-offsets
  [inst metro beat beat-offsets]
  (dorun
   (map (fn [[offset args]]
          (when-not (some nil? (vals args))
            (at (metro (+ beat offset)) (inst args))))
        (partition 2 beat-offsets))))

(defn play-notes-with-offsets
  [inst metro beat notes beat-offsets]
  (dorun
   (map (fn [note offset]
          (at (metro (+ beat offset)) (inst {:note note})))
        notes
        beat-offsets)))

;;this assumes you have the mda-piano available. Feel free to replace piano with
;;a different synth which accepts a MIDI note as its first arg such as tb303.
;;(def instrument tb303)
(defn beat-loop
  [metro beat chord-idx]
  (let [[tonic chord-name] (choose (seq (nth chord-prog chord-idx)))
        tonic              (+ root tonic)
        max-range          (cosr beat range-variation max-range range-period)
        notes              (rand-chord tonic chord-name (count beat-offsets) max-range)
        nxt-chord-idx      (mod (inc chord-idx) (count chord-prog))]
    (play-with-offsets tb303 metro beat
      [0.1 {:note (first notes)}
       0.1 {:note (nth notes 4)}])

    (play-with-offsets kick metro beat
      [0.1 {}
       0.5 {:volume 1.4}
       0.7 {:volume 1.4}])

    (play-with-offsets snare metro beat
      [0.1 {}
       0.2 {}
       0.25 {:volume 1.2}])

    (play-with-offsets c-hat metro beat
      [0.1 {}
       0.3 {}
       0.5 {}
       0.7 {}
       0.9 {}])

    (play-notes-with-offsets instrument metro beat notes beat-offsets)

    (apply-at (metro (inc beat)) #'beat-loop [metro (inc beat) nxt-chord-idx])))

;;start the music:
(beat-loop metro (metro) 0)
(tick)
;;try changing the beat-offsets on the fly
;;(def beat-offsets [0 0.2 1/3  0.5 0.8])
;;(def beat-offsets [0 0.2 0.4  0.6 0.8])
;;(def beat-offsets [0 0.1 0.11 0.13 0.15 0.17 0.2 0.4 0.5 0.55 0.6 0.8])

;;to stop, define beat-loop to not schedule another callback:
;;(defn beat-loop [m b r]nil)