import argparse
import os
import re
import gitlab

def parse_markdown(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract title - assumes the first line is the title starting with '# '
    title_match = re.search(r'^#\s+(.*?)$', content, re.MULTILINE)
    title = title_match.group(1).strip() if title_match else os.path.basename(filepath)

    # Extract description and acceptance criteria
    desc_match = re.search(r'##\s+Description\s+(.*?)(?=##\s+Acceptance Criteria|\Z)', content, re.DOTALL | re.IGNORECASE)
    description = desc_match.group(1).strip() if desc_match else ""

    ac_match = re.search(r'##\s+Acceptance Criteria\s+(.*)', content, re.DOTALL | re.IGNORECASE)
    acceptance_criteria = ac_match.group(1).strip() if ac_match else ""

    # Reconstruct the body to preserve the markdown structure
    item_description = ""
    if description:
        item_description += f"## Description\n\n{description}\n\n"
    if acceptance_criteria:
        item_description += f"## Acceptance Criteria\n\n{acceptance_criteria}"

    return title, item_description

def main():
    parser = argparse.ArgumentParser(description="Import markdown files as GitLab work items.")
    parser.add_argument('--folder', required=True, help="Path to the local folder containing markdown files.")
    parser.add_argument('--project', required=True, help="GitLab Project ID or Path (e.g., 'namespace/project').")
    parser.add_argument('--milestone', help="GitLab Milestone Title.")
    parser.add_argument('--type', default="task", choices=["issue", "incident", "test_case", "task"], help="Type of work item to create (default: task).")
    parser.add_argument('--url', default="https://gitlab.com", help="GitLab instance URL (default: https://gitlab.com).")
    parser.add_argument('--token', required=True, help="GitLab Personal Access Token.")
    
    args = parser.parse_args()

    if not os.path.isdir(args.folder):
        print(f"Error: Folder '{args.folder}' does not exist.")
        return

    # Initialize GitLab connection
    try:
        gl = gitlab.Gitlab(args.url, private_token=args.token)
        gl.auth()
    except Exception as e:
        print(f"Error authenticating with GitLab: {e}")
        return
    
    try:
        project = gl.projects.get(args.project)
        print(f"Found project: {project.name}")
    except gitlab.exceptions.GitlabGetError:
        print(f"Error: Project '{args.project}' not found or you don't have access.")
        return

    milestone_id = None
    if args.milestone:
        milestones = project.milestones.list(title=args.milestone)
        if milestones:
            milestone_id = milestones[0].id
            print(f"Found milestone: {args.milestone}")
        else:
            print(f"Warning: Milestone '{args.milestone}' not found in project. Work items will be created without a milestone.")

    # Process markdown files
    for filename in os.listdir(args.folder):
        if filename.lower().endswith(".md"):
            filepath = os.path.join(args.folder, filename)
            title, description = parse_markdown(filepath)
            
            print(f"Processing ({args.type}): {title}")
            
            # Check if a work item with this title already exists
            try:
                existing_items = project.issues.list(search=title)
                existing_item = next((item for item in existing_items if item.title.strip().lower() == title.strip().lower()), None)
                
                if existing_item:
                    print(f"  -> Skipped: Work item already exists ({existing_item.web_url})")
                    continue
            except Exception as e:
                print(f"  -> Warning: Failed to check for existing work item: {e}")
            
            print(f"  -> Creating new work item...")
            work_item_data = {
                'title': title,
                'description': description,
                'issue_type': args.type
            }
            if milestone_id:
                work_item_data['milestone_id'] = milestone_id
                
            try:
                # Note: In GitLab REST API, work items are created via the issues endpoint by specifying the type
                work_item = project.issues.create(work_item_data)
                print(f"  -> Created successfully: {work_item.web_url}")
            except Exception as e:
                print(f"  -> Failed to create work item: {e}")

if __name__ == "__main__":
    main()
