#!/usr/bin/env python3
"""Compatibility entrypoint: keeps approved fidelity geometry and only regenerates Java/texture artifacts."""
from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).with_name('regenerate-fidelity-models.py')), run_name='__main__')
