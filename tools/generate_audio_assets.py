#!/usr/bin/env python3
"""
Procedural Original Audio Asset Generator for Anime Arena Legends.
Generates clean 22050Hz 16-bit mono PCM .wav files for:
- Background Music (Menu, Battle Normal, Battle Intense, Ultimate Cinematic, Victory, Defeat)
- Sound Effects (Light, Heavy, Block, Dodge, Footstep, Jump, Charge, Release, Lightning, Fire, UltImpact, Whoosh, UI, Victory, Defeat)
- Original Formant-Synthesized Character Voice Lines for Raiden & Homura (zero copyrighted imitation)
"""

import math
import os
import random
import struct
import wave

OUT_DIR = os.path.join("app", "src", "main", "assets", "audio")
os.makedirs(OUT_DIR, exist_ok=True)
SR = 22050


def save_wav(filename: str, samples: list[float], peak_limit: float = 0.85):
    path = os.path.join(OUT_DIR, filename)
    max_amp = max((abs(s) for s in samples), default=1.0)
    scale = (peak_limit / max_amp) if max_amp > 1e-6 else 1.0
    if max_amp < peak_limit:
        scale = min(scale, 1.0 / max(max_amp, 0.01) * peak_limit)
    pcm = bytearray()
    for s in samples:
        v = max(-1.0, min(1.0, s * scale))
        pcm.extend(struct.pack("<h", int(v * 32767)))
    with wave.open(path, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(SR)
        wf.writeframes(bytes(pcm))


def env_ad(t: float, dur: float, a: float = 0.01, d: float = 0.1) -> float:
    if t < a:
        return t / max(1e-5, a)
    rem = dur - t
    if rem < d:
        return max(0.0, rem / max(1e-5, d))
    return 1.0


def synth_formant_voice(dur: float, f0_start: float, f0_end: float, formants: list[tuple[float, float, float]], vibrato: float = 0.0, rasp: float = 0.08):
    n = int(SR * dur)
    out = [0.0] * n
    rng = random.Random(int(f0_start * 100 + dur * 1000))
    phase = 0.0
    for i in range(n):
        t = i / SR
        p = t / max(1e-5, dur)
        f0 = f0_start + (f0_end - f0_start) * p + math.sin(2 * math.pi * 6.0 * t) * vibrato
        phase += 2 * math.pi * f0 / SR
        # Glottal pulse harmonics + formant resonance approximation
        glottal = 0.0
        for h in range(1, 14):
            freq_h = f0 * h
            gain = 1.0 / (h ** 0.75)
            f_gain = 0.15
            for fc, bw, fg in formants:
                diff = (freq_h - fc) / bw
                f_gain += fg / (1.0 + diff * diff)
            glottal += math.sin(phase * h) * gain * f_gain
        breath = (rng.random() * 2.0 - 1.0) * rasp * math.exp(-p * 2.2)
        amp = env_ad(t, dur, a=0.025, d=max(0.06, dur * 0.25))
        out[i] = (glottal * 0.28 + breath) * amp
    return out


def concat_segments(segs: list[list[float]], gap_sec: float = 0.03):
    gap = [0.0] * int(SR * gap_sec)
    out = []
    for idx, s in enumerate(segs):
        out.extend(s)
        if idx < len(segs) - 1:
            out.extend(gap)
    return out


# ---------------------------------------------------------------------------
# 1. Sound Effects
# ---------------------------------------------------------------------------
def gen_sfx():
    rng = random.Random(42)

    # Light punch / slash
    dur = 0.18
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        tone = math.sin(2 * math.pi * (420 * math.exp(-t * 16) + 95) * t) * math.exp(-t * 22)
        snap = (rng.random() * 2 - 1) * math.exp(-t * 36) * 0.8
        s.append((tone + snap) * env_ad(t, dur, 0.004, 0.05))
    save_wav("sfx_light_hit.wav", s)

    # Heavy impact
    dur = 0.38
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        sub = math.sin(2 * math.pi * (170 * math.exp(-t * 9) + 46) * t) * math.exp(-t * 8)
        crunch = (rng.random() * 2 - 1) * math.exp(-t * 15) * 0.95
        s.append((sub * 1.2 + crunch) * env_ad(t, dur, 0.005, 0.1))
    save_wav("sfx_heavy_impact.wav", s)

    # Block clang
    dur = 0.28
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        clang = (math.sin(2 * math.pi * 880 * t) + 0.7 * math.sin(2 * math.pi * 1340 * t) + 0.5 * math.sin(2 * math.pi * 2180 * t)) * math.exp(-t * 14)
        s.append(clang * env_ad(t, dur, 0.002, 0.08))
    save_wav("sfx_block.wav", s)

    # Dodge whoosh
    dur = 0.22
    s = []
    lp = 0.0
    for i in range(int(SR * dur)):
        t = i / SR
        lp = lp * 0.78 + (rng.random() * 2 - 1) * 0.22
        sweep = math.sin(2 * math.pi * (520 - 300 * (t / dur)) * t) * 0.25
        s.append((lp * 1.4 + sweep) * env_ad(t, dur, 0.02, 0.1))
    save_wav("sfx_dodge.wav", s)

    # Footstep
    dur = 0.10
    s = [(rng.random() * 2 - 1) * math.exp(-i / SR * 40) * 0.4 + math.sin(2 * math.pi * 110 * (i / SR)) * math.exp(-i / SR * 30) for i in range(int(SR * dur))]
    save_wav("sfx_footstep.wav", s)

    # Jump
    dur = 0.22
    s = [math.sin(2 * math.pi * (180 + 380 * (i / SR / dur)) * (i / SR)) * env_ad(i / SR, dur, 0.01, 0.09) for i in range(int(SR * dur))]
    save_wav("sfx_jump.wav", s)

    # Energy charging
    dur = 0.85
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        p = t / dur
        f = 160 + 480 * p * p
        hum = math.sin(2 * math.pi * f * t) + 0.5 * math.sin(2 * math.pi * f * 1.5 * t)
        shimmer = math.sin(2 * math.pi * 18 * t) * 0.3 + 0.7
        s.append(hum * shimmer * env_ad(t, dur, 0.08, 0.12))
    save_wav("sfx_energy_charge.wav", s)

    # Energy release
    dur = 0.45
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        boom = math.sin(2 * math.pi * (420 * math.exp(-t * 7) + 60) * t) * math.exp(-t * 6)
        air = (rng.random() * 2 - 1) * math.exp(-t * 8) * 0.6
        s.append((boom + air) * env_ad(t, dur, 0.005, 0.12))
    save_wav("sfx_energy_release.wav", s)

    # Lightning strike / crackle
    dur = 0.52
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        z = (1.0 if math.sin(2 * math.pi * (920 + 400 * math.sin(t * 75)) * t) > 0 else -1.0) * 0.4
        crack = (rng.random() * 2 - 1) * (0.9 if int(t * 35) % 3 == 0 else 0.35)
        s.append((z + crack) * math.exp(-t * 5.5) * env_ad(t, dur, 0.003, 0.15))
    save_wav("sfx_lightning.wav", s)

    # Fire whoosh & roar
    dur = 0.55
    s = []
    lp = 0.0
    for i in range(int(SR * dur)):
        t = i / SR
        lp = lp * 0.86 + (rng.random() * 2 - 1) * 0.14
        roar = math.sin(2 * math.pi * (115 + 40 * math.sin(t * 14)) * t) * 0.5
        s.append((lp * 2.2 + roar) * env_ad(t, dur, 0.04, 0.22))
    save_wav("sfx_fire.wav", s)

    # Ultimate impact
    dur = 0.95
    s = []
    for i in range(int(SR * dur)):
        t = i / SR
        sub = math.sin(2 * math.pi * (140 * math.exp(-t * 4) + 38) * t) * math.exp(-t * 3.2)
        ring = math.sin(2 * math.pi * 620 * t) * math.exp(-t * 6) * 0.35
        blast = (rng.random() * 2 - 1) * math.exp(-t * 4.5) * 0.8
        s.append((sub * 1.4 + ring + blast) * env_ad(t, dur, 0.005, 0.25))
    save_wav("sfx_ultimate_impact.wav", s)

    # Cinematic transition
    dur = 0.5
    s = [math.sin(2 * math.pi * (90 + 620 * (1 - i / SR / dur)) * (i / SR)) * 0.5 + (rng.random() * 2 - 1) * 0.3 * env_ad(i / SR, dur, 0.02, 0.18) for i in range(int(SR * dur))]
    save_wav("sfx_cinematic_whoosh.wav", s)

    # UI click
    dur = 0.07
    s = [(math.sin(2 * math.pi * 980 * (i / SR)) + 0.5 * math.sin(2 * math.pi * 1470 * (i / SR))) * math.exp(-i / SR * 55) for i in range(int(SR * dur))]
    save_wav("sfx_ui_click.wav", s)

    # Victory / Defeat stingers
    notes_v = [523.25, 659.25, 783.99, 1046.5]
    save_wav("sfx_victory.wav", concat_segments([
        [math.sin(2 * math.pi * f * (i / SR)) * env_ad(i / SR, 0.18, 0.01, 0.08) for i in range(int(SR * 0.18))]
        for f in notes_v
    ], 0.01))

    notes_d = [392.0, 349.23, 311.13, 261.63]
    save_wav("sfx_defeat.wav", concat_segments([
        [math.sin(2 * math.pi * f * (i / SR)) * env_ad(i / SR, 0.24, 0.01, 0.12) for i in range(int(SR * 0.24))]
        for f in notes_d
    ], 0.01))


# ---------------------------------------------------------------------------
# 2. Original Character Voice Lines (Formant Synthesis)
# ---------------------------------------------------------------------------
def gen_voices():
    # Raiden: crisp agile tenor (185-280 Hz), brighter formants
    F_A = [(720, 90, 1.0), (1250, 120, 0.8), (2550, 180, 0.4)]
    F_I = [(340, 70, 1.0), (2200, 150, 0.9), (2900, 200, 0.4)]
    F_O = [(460, 80, 1.0), (920, 110, 0.7), (2450, 180, 0.3)]
    F_E = [(480, 80, 1.0), (1850, 140, 0.8), (2600, 180, 0.4)]

    save_wav("vo_raiden_intro.wav", concat_segments([
        synth_formant_voice(0.24, 195, 225, F_E),
        synth_formant_voice(0.32, 220, 200, F_I, vibrato=4.0),
    ], 0.05))
    save_wav("vo_raiden_idle.wav", synth_formant_voice(0.35, 190, 180, F_O, rasp=0.14))
    save_wav("vo_raiden_light.wav", synth_formant_voice(0.16, 240, 290, F_A, rasp=0.05))
    save_wav("vo_raiden_heavy.wav", synth_formant_voice(0.28, 220, 320, F_A, rasp=0.12))
    save_wav("vo_raiden_special.wav", concat_segments([
        synth_formant_voice(0.18, 230, 265, F_I),
        synth_formant_voice(0.34, 265, 330, F_A, rasp=0.1),
    ], 0.03))
    save_wav("vo_raiden_ult_prep.wav", concat_segments([
        synth_formant_voice(0.28, 200, 245, F_O),
        synth_formant_voice(0.38, 245, 285, F_E, vibrato=5.0),
    ], 0.05))
    save_wav("vo_raiden_ult_shout.wav", synth_formant_voice(0.68, 275, 365, F_A, vibrato=6.0, rasp=0.15))
    save_wav("vo_raiden_hit.wav", synth_formant_voice(0.22, 235, 165, F_E, rasp=0.22))
    save_wav("vo_raiden_victory.wav", concat_segments([
        synth_formant_voice(0.24, 210, 240, F_A),
        synth_formant_voice(0.36, 235, 215, F_I, vibrato=4.0),
    ], 0.05))
    save_wav("vo_raiden_defeat.wav", synth_formant_voice(0.48, 195, 130, F_O, rasp=0.22))

    # Homura: fierce resonant baritone (135-235 Hz), deeper formants
    D_A = [(650, 95, 1.0), (1120, 120, 0.8), (2350, 180, 0.4)]
    D_O = [(420, 85, 1.0), (820, 105, 0.8), (2250, 180, 0.3)]
    D_U = [(320, 75, 1.0), (760, 100, 0.7), (2200, 170, 0.3)]

    save_wav("vo_homura_intro.wav", concat_segments([
        synth_formant_voice(0.26, 145, 168, D_O),
        synth_formant_voice(0.34, 165, 145, D_A, vibrato=3.5),
    ], 0.05))
    save_wav("vo_homura_idle.wav", synth_formant_voice(0.38, 138, 128, D_U, rasp=0.16))
    save_wav("vo_homura_light.wav", synth_formant_voice(0.18, 168, 215, D_A, rasp=0.08))
    save_wav("vo_homura_heavy.wav", synth_formant_voice(0.32, 155, 235, D_O, rasp=0.15))
    save_wav("vo_homura_special.wav", concat_segments([
        synth_formant_voice(0.20, 160, 195, D_U),
        synth_formant_voice(0.36, 195, 245, D_A, rasp=0.14),
    ], 0.03))
    save_wav("vo_homura_ult_prep.wav", concat_segments([
        synth_formant_voice(0.30, 145, 175, D_U),
        synth_formant_voice(0.40, 175, 210, D_O, vibrato=4.5),
    ], 0.05))
    save_wav("vo_homura_ult_shout.wav", synth_formant_voice(0.72, 195, 265, D_A, vibrato=5.5, rasp=0.18))
    save_wav("vo_homura_hit.wav", synth_formant_voice(0.24, 175, 120, D_U, rasp=0.25))
    save_wav("vo_homura_victory.wav", concat_segments([
        synth_formant_voice(0.26, 155, 175, D_O),
        synth_formant_voice(0.38, 172, 150, D_A, vibrato=3.5),
    ], 0.05))
    save_wav("vo_homura_defeat.wav", synth_formant_voice(0.52, 145, 98, D_U, rasp=0.24))


# ---------------------------------------------------------------------------
# 3. Original Looping Background Music (Japanese Pentatonic Anime Action)
# ---------------------------------------------------------------------------
def gen_bgm():
    # D minor pentatonic / Hirajoshi inspired frequencies
    # Menu BGM (8s loop, atmospheric koto pluck + warm twilight pad)
    dur_menu = 8.0
    n_menu = int(SR * dur_menu)
    menu = [0.0] * n_menu
    scale_menu = [293.66, 329.63, 349.23, 440.0, 466.16, 587.33, 440.0, 349.23]
    for i in range(n_menu):
        t = i / SR
        pad = 0.16 * math.sin(2 * math.pi * 146.83 * t) + 0.11 * math.sin(2 * math.pi * 220.0 * t)
        step = int(t * 2.0) % len(scale_menu)
        lt = (t * 2.0) % 1.0 * 0.5
        pluck_f = scale_menu[step]
        pluck = (math.sin(2 * math.pi * pluck_f * lt) + 0.45 * math.sin(2 * math.pi * pluck_f * 2 * lt)) * math.exp(-lt * 6.5) * 0.28
        menu[i] = pad + pluck
    save_wav("bgm_menu.wav", menu, peak_limit=0.72)

    # Battle BGM Normal (8s seamless loop at 120 BPM: taiko + shamisen-synth + bass)
    dur_bat = 8.0
    n_bat = int(SR * dur_bat)
    bat_norm = [0.0] * n_bat
    bat_int = [0.0] * n_bat
    riff = [293.66, 349.23, 440.0, 392.0, 349.23, 329.63, 293.66, 261.63,
            293.66, 440.0, 523.25, 440.0, 392.0, 349.23, 329.63, 440.0]
    for i in range(n_bat):
        t = i / SR
        beat_t = (t * 2.0) % 1.0 * 0.5  # 120 BPM quarter
        eighth_idx = int(t * 4.0) % len(riff)
        eighth_t = (t * 4.0) % 1.0 * 0.25
        # Taiko drum
        kick = math.sin(2 * math.pi * (120 * math.exp(-beat_t * 14) + 52) * beat_t) * math.exp(-beat_t * 9.0) * 0.45
        # Bassline
        bf = 73.42 if (int(t * 0.5) % 2 == 0) else 65.41
        bass = math.sin(2 * math.pi * bf * t) * 0.24
        # Shamisen-like lead
        lf = riff[eighth_idx]
        lead = (math.sin(2 * math.pi * lf * eighth_t) + 0.5 * math.sin(2 * math.pi * lf * 2 * eighth_t)) * math.exp(-eighth_t * 10.0) * 0.30
        bat_norm[i] = kick + bass + lead

        # Intense version adds octave lead + driving 16th hi-hat pulse
        sixteenth_t = (t * 8.0) % 1.0 * 0.125
        drive = math.sin(2 * math.pi * lf * 2 * sixteenth_t) * math.exp(-sixteenth_t * 14.0) * 0.22
        sub_oct = math.sin(2 * math.pi * bf * 2 * t) * (0.5 + 0.5 * math.sin(2 * math.pi * 8 * t)) * 0.18
        bat_int[i] = bat_norm[i] + drive + sub_oct

    save_wav("bgm_battle.wav", bat_norm, peak_limit=0.75)
    save_wav("bgm_battle_intense.wav", bat_int, peak_limit=0.78)

    # Ultimate Cinematic BGM (4s dramatic rising choir/brass synth chord)
    dur_ult = 4.0
    n_ult = int(SR * dur_ult)
    ult = [0.0] * n_ult
    for i in range(n_ult):
        t = i / SR
        p = t / dur_ult
        chord = (
            math.sin(2 * math.pi * 146.83 * t)
            + 0.8 * math.sin(2 * math.pi * 220.0 * t)
            + 0.7 * math.sin(2 * math.pi * 293.66 * t)
            + 0.6 * math.sin(2 * math.pi * (440.0 + 110.0 * p) * t)
        )
        pulse = 0.75 + 0.25 * math.sin(2 * math.pi * 6.0 * t)
        ult[i] = chord * pulse * env_ad(t, dur_ult, 0.1, 0.3) * 0.3
    save_wav("bgm_ultimate.wav", ult, peak_limit=0.80)

    # Victory BGM (5s uplifting major fanfare loop)
    dur_vic = 5.0
    n_vic = int(SR * dur_vic)
    vic = [0.0] * n_vic
    v_notes = [293.66, 369.99, 440.0, 587.33, 587.33, 659.25, 587.33, 440.0]
    for i in range(n_vic):
        t = i / SR
        idx = int(t * (len(v_notes) / dur_vic)) % len(v_notes)
        lt = (t * (len(v_notes) / dur_vic)) % 1.0
        f = v_notes[idx]
        pad = 0.22 * math.sin(2 * math.pi * 146.83 * t) + 0.18 * math.sin(2 * math.pi * 220.0 * t)
        mel = (math.sin(2 * math.pi * f * t) + 0.35 * math.sin(2 * math.pi * f * 2 * t)) * math.exp(-lt * 2.5) * 0.32
        vic[i] = (pad + mel) * env_ad(t, dur_vic, 0.05, 0.25)
    save_wav("bgm_victory.wav", vic, peak_limit=0.75)

    # Defeat BGM (5s somber minor resolution)
    dur_def = 5.0
    n_def = int(SR * dur_def)
    deff = [0.0] * n_def
    d_notes = [293.66, 261.63, 220.0, 196.0, 174.61]
    for i in range(n_def):
        t = i / SR
        idx = int(t) % len(d_notes)
        lt = t % 1.0
        f = d_notes[idx]
        deff[i] = (math.sin(2 * math.pi * f * t) + 0.3 * math.sin(2 * math.pi * (f * 0.5) * t)) * math.exp(-lt * 1.4) * env_ad(t, dur_def, 0.08, 0.4) * 0.35
    save_wav("bgm_defeat.wav", deff, peak_limit=0.70)


if __name__ == "__main__":
    gen_sfx()
    gen_voices()
    gen_bgm()
    print("Generated all SFX, Voice, and BGM .wav assets in", OUT_DIR)
