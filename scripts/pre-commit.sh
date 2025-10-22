#!/bin/bash

echo "Running pre-commit checks..."

# Check wrapper is executable
if [ ! -x "./mvnw" ]; then
    chmod +x ./mvnw
fi

# Run tests
echo "Running unit tests..."
./mvnw test -q

if [ $? -ne 0 ]; then
    echo "Tests failed! Please fix before committing."
    exit 1
fi

# Check code style
echo "Checking code style..."
./mvnw checkstyle:check -q

if [ $? -ne 0 ]; then
    echo "Checkstyle violations found! Please fix before committing."
    exit 1
fi

# Check for pendent comments in staged files
echo "Checking for TODO comments..."
if git diff --cached --name-only | xargs grep -n "TODO" 2>/dev/null; then
    echo "TODO comments found in committed code! Please remove or address them."
    exit 1
fi

echo "All pre-commit checks passed!"
exit 0