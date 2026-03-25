# MetaChain
A simple blockchain implementation built with Java. This project demonstrates the core concepts behind how blockchains work — from cryptographic hashing to chained block structures — implemented from scratch as a learning exercise.

## What It Does
Metachain simulates the fundamental mechanics of a blockchain:
- Creates blocks that each store data and a timestamp
- Links blocks together using the hash of the previous block
- Validates the integrity of the chain by re-checking all hashes

## What I Learned
- Data structures — understanding how a chain of linked blocks forms an immutable ledger
- Cryptographic hashing — using SHA-256 to fingerprint block data and detect tampering
- Object-oriented design — modeling real-world concepts (blocks, chains, transactions) as Java classes
- Core Java — working with classes, loops, and the standard library without any frameworks

## Installation
1. Clone the repository:
```
bash   git clone https://github.com/faiknet/metachain.git
```
2. Open Eclipse and go to File → Import → Existing Projects into Workspace
3. Select the cloned metachain folder and click Finish
4. Run the project with Run → Run As → Java Application
