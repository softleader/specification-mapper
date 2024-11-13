import os
import shutil
import argparse


def find_readme_files(base_dir):
    readme_files = []
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.lower() == "readme.md":
                folder_name = os.path.basename(root)
                readme_files.append((folder_name, os.path.join(root, file)))
    return readme_files


def copy_readme_files(readme_files, destination_dir):
    if not os.path.exists(destination_dir):
        os.makedirs(destination_dir)

    for folder_name, file_path in readme_files:
        new_file_name = f"{folder_name}.md"
        destination_path = os.path.join(destination_dir, new_file_name)

        # 讀取原始 README.md 檔案內容
        with open(file_path, 'r', encoding='utf-8') as original_file:
            original_content = original_file.read()

        # 添加檔案開頭的 YAML 標頭
        header = f"---\ntitle: {folder_name}\nweight: \ndescription: >\n---\n\n"
        new_content = header + original_content

        # 將新內容寫入複製後的檔案
        with open(destination_path, 'w', encoding='utf-8') as new_file:
            new_file.write(new_content)

        print(f"已複製 {file_path} 至 {destination_path}，並添加了 YAML 標頭")


def main():
    parser = argparse.ArgumentParser(description="處理 README.md 檔案並複製到指定目錄")
    parser.add_argument("base_dir", type=str, help="專案的根目錄路徑")
    parser.add_argument("destination_dir", type=str, help="複製檔案的目標目錄")
    args = parser.parse_args()

    readme_files = find_readme_files(args.base_dir)
    copy_readme_files(readme_files, args.destination_dir)


if __name__ == "__main__":
    main()
