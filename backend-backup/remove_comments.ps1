$sourceDir = "c:\Users\Bishoy\Desktop\Loyalixa 3\backend\src\main\java"

Get-ChildItem -Path $sourceDir -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    # Regex explanation:
    # Group 1: Double quoted strings OR Single quoted strings (handling escapes)
    # Group 2: Single line comments // ...
    # Group 3: Multi line comments /* ... */
    $pattern = '("(?:\\[\s\S]|[^"\\])*"|''(?:\\[\s\S]|[^''\\])*'')|(\/\/.*)|(\/\*[\s\S]*?\*\/)'
    
    $newContent = [Regex]::Replace($content, $pattern, {
        param($match)
        if ($match.Groups[1].Success) {
            return $match.Groups[1].Value # It's a string, keep it
        }
        # It's a comment.
        # If it's a single line comment (Group 2), we might want to keep the newline if it was matched?
        # The regex `//.*` matches until end of line but usually excludes the newline character itself in `.` unless SingleLine mode is on.
        # Here we are using Multiline mode, `.` does not match newline.
        # So `//.*` matches the comment content. The newline remains in the file content after the match.
        # Replacing with empty string is fine for `//`.
        # For `/* */`, replacing with a space is safer to avoid sticking code together.
        
        if ($match.Groups[2].Success) {
            return "" 
        }
        return " "
    })

    if ($content -ne $newContent) {
        # Use UTF8 encoding to preserve characters
        [System.IO.File]::WriteAllText($_.FullName, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Processed $($_.Name)"
    }
}
