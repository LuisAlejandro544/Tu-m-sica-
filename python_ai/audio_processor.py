#!/usr/bin/env python3
"""
SpotLocal Python AI - Audio Processing & Stem Separation Math Utilities
"""

import os
from model_config import SAMPLE_RATE, STEREO_CHANNELS

class AudioProcessor:
    def __init__(self, sample_rate=SAMPLE_RATE, channels=STEREO_CHANNELS):
        self.sample_rate = sample_rate
        self.channels = channels

    fun_prepare_paths = None

    def prepare_output_paths(self, input_audio_path, output_dir):
        os.makedirs(output_dir, exist_ok=True)
        base_name = os.path.splitext(os.path.basename(input_audio_path))[0]
        vocals_path = os.path.join(output_dir, f"{base_name}_vocals.wav")
        instrumental_path = os.path.join(output_dir, f"{base_name}_instrumental.wav")
        return vocals_path, instrumental_path

    def compute_stem_gains(self, stem_mode):
        gains = {
            "ORIGINAL": (0.0, 0.0),
            "VOCALS_ONLY": (0.0, -60.0),
            "INSTRUMENTAL": (-60.0, 0.0),
            "KARAOKE": (-14.0, 2.0)
        }
        return gains.get(stem_mode, (0.0, 0.0))
