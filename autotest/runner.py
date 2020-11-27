import concurrent.futures
import datetime
import subprocess
from pathlib import Path

jar_file = Path("aqmaps-0.0.1-SNAPSHOT.jar")

start = datetime.datetime.strptime("01-01-2020", "%d-%m-%Y")
end = datetime.datetime.strptime("01-02-2020", "%d-%m-%Y")
date_generated = (
    start + datetime.timedelta(days=x) for x in range(0, (end - start).days)
)

starting_lat = "55.9444"
starting_lng = "-3.1878"
random_state = "5678"
port = "8989"


def autorun(arg):
    try:
        result = subprocess.run(arg, capture_output=True, text=True, timeout=60)
        f_name = "readings-" + "-".join(arg[3:6])  + ".geojson"
        if result.stdout == "Done!\n" :
            with open("okay.txt", "a+") as f:
                f.write(f"{f_name} is okay\n")
        else:
            with open("not-okay.txt", "a+") as f:
                f.write(f"{f_name} is not okay\n")
    except Exception as e:
        with open("timeout.txt", "a+") as f:
            date = '-'.join(arg[3:6])
            f.write(f"{date} time out, probably an infinite loop somewhere.\n")


def generate_arg_list():
    arg_list = []
    for date in date_generated:
        date_str = date.strftime("%d-%m-%Y").split("-")
        cmd = [
                "java",
                "-jar",
                str(jar_file.resolve()),
                date_str[0],
                date_str[1],
                date_str[2],
                starting_lat,
                starting_lng,
                random_state,
                port,
            ]
        arg_list.append(cmd)
    return arg_list

if __name__ == "__main__":
    arg_list = generate_arg_list()
    okay_f = open("okay.txt", "w")
    not_okay_f = open("not-okay.txt", "w")
    timeout_f = open("timeout.txt", "w")
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
        for arg in arg_list:
            executor.submit(autorun, arg)
    
    okay_f.close()
    not_okay_f.close()
    timeout_f.close()