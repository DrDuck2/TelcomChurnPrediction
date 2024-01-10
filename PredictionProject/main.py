import os
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Set the directory path
directory_path = r'C:\Users\Dominik\PycharmProjects\PredictionProject\reports'

# Specify the file names
file_names = ["lab_data.csv", "con_data.csv", "con_fil.csv", "con_sel.csv", "lab_fil.csv", "lab_sel.csv"]

# Load data from CSV files
dfs = [pd.read_csv(os.path.join(directory_path, file)) for file in file_names]

# Plotting
sns.set(style="whitegrid")

# Define models and metrics
models = dfs[0]["Source"].unique()
metrics = ["AUC", "Accuracy", "Precision", "Recall", "F1"]

# Set up colors for different metrics
metric_colors = sns.color_palette("husl", n_colors=len(metrics))

# Set the width for each bar
bar_width = 0.15

# Plot grouped bar plot for each metric
for i, metric in enumerate(metrics):
    plt.figure(figsize=(15, 7))

    max_file_name = ''

    max_y_value = 0

    for j, model in enumerate(models):
        values = [df[df["Source"] == model][metric].values[0] for df in dfs]
        plt.bar([x + j * bar_width for x in range(len(file_names))], values, label=model, color=metric_colors[j],
                width=bar_width)

        plt.ylim(min(values)-0.09, max_y_value+0.09)

        max_value_for_model = max(values)
        if max_value_for_model > max_y_value:
            max_y_value = max_value_for_model
            max_file_name = file_names[values.index(max_value_for_model)]


    plt.axhline(y=max_y_value, color='red', linestyle='--', label=f'Max {metric}')

    # Add text with the file name on top of the Y line
    plt.text(len(file_names)-1.12, max_y_value + 0.001, f' {max_file_name}: {max_y_value:.2f}', color='red', ha='left', va='bottom')
    plt.text(len(file_names) + 0.4, max_y_value + 0.1, 'Note: Max value is rounded', color='red', ha='right', va='bottom')
    # Add a disclaimer at the bottom-right
    plt.xlabel('Files')
    plt.ylabel(metric)
    plt.title(f"Performance Metrics for {metric} across Files")
    plt.xticks([x + bar_width for x in range(len(file_names))], file_names)  # Adjusted the positioning
    plt.legend(title="Models", bbox_to_anchor=(0.95, 1.05), loc='upper left')

    plt.savefig(fr'C:\Users\Dominik\PycharmProjects\PredictionProject\reports\images\plot_{metric}.png', bbox_inches='tight')

    plt.show()