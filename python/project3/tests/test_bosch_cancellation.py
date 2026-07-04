import pandas as pd
import pytest

from algorithms.bosch_services import run_bosch_key_station, run_bosch_kqc_mining
from task_manager import TaskCanceledError


def test_bosch_kqc_mining_cooperatively_cancels_after_input_validation(tmp_path):
    source = tmp_path / "train_numeric.csv"
    pd.DataFrame({
        "Id": [1, 2],
        "L3_S36_F3939": [0.1, 0.2],
        "Response": [0, 1],
    }).to_csv(source, index=False)

    with pytest.raises(TaskCanceledError, match="KQC input validation"):
        run_bosch_kqc_mining(
            train_numeric_csv=str(source),
            output_dir=tmp_path / "out",
            cancel_check=lambda: True,
        )


def test_bosch_key_station_cooperatively_cancels_before_matrix_loading(tmp_path):
    adj = tmp_path / "adj.csv"
    freq = tmp_path / "freq.csv"
    pd.DataFrame([[0.0]], index=["Response"], columns=["Response"]).to_csv(adj)
    pd.DataFrame([[0.0]], index=["Response"], columns=["Response"]).to_csv(freq)

    with pytest.raises(TaskCanceledError, match="key station input validation"):
        run_bosch_key_station(
            adj_path=str(adj),
            freq_path=str(freq),
            output_dir=tmp_path / "out",
            cancel_check=lambda: True,
        )
