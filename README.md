# recordsapi

Simple data-parsing CLI/API


## Usage

### As CLI

**Show help screen:**

```bash
$ lein run -- --help
```


**Import/view data:**

```bash
$ lein run -- --file $INPUT_PATH --view 1
```

Where `view` can be one of:

- **1**: sort data by email address descending, then last name ascending
- **2**: sort data by birthdate ascending
- **3**: sort data by last name descending


Data to import must be text files with the following format:

| Last name | First name | Email address | Favorite color | Birthdate  |
| ---       | ---        | ---           | ---            | ---        |
| Smith     | Sarah      | s@hotmail.com | mauve          | 1980-08-22 |

Record separators can be either `, ` (comma+space), ` | ` (space+pipe+space), or `" "` (space). The separator must be consistent throughout a given file.

Sample data in all supported formats is available in `./resources`:

| Filename | Format          |
| ---      | ---             |
| data.csv | Comma-separated |
| data.psv | Pipe-separated  |
| data.ssv | Space-separated |


Dates must be ISO-8601 format (`yyyy-MM-dd`).


### As web server

**Start the server:**

```bash
$ lein run -- --server
```

As with the CLI, you may optionally load a file here via `--file`.


**Consume the API:**

| Action | Shell command |
| ---    | ---      |
| Get records sorted by email | `curl http://localhost:3030/records/email` |
| Get records sorted by birthdate | `curl http://localhost:3030/records/birthdate` |
| Get records sorted by last name | `curl http://localhost:3030/records/name` |
| Add a record (comma-separated values) | `curl -H 'Content-Type: application/json' -d 'line="foo, bar, foobar@example.com, red, 1973-03-22"' http://localhost:3030/records` |
| Add a record (pipe-separated values) | `curl -H 'Content-Type: application/json' -d 'line="baz | quux | qb@example.com | jade | 1986-10-31"' http://localhost:3030/records` |
| Add a record (space-separated values) | `curl -H 'Content-Type: application/json' -d '{"line":"schmoe joe joe@me.com violet 1953-12-13"}' http://localhost:3030/records` |

*Note*: [HTTPie](https://httpie.io/) is a more user-friendly CLI web client than cURL, and will display JSON/response codes in a more pleasing and informative way.


### Run tests

```bash
$ lein test
```


## License

Proprietary

Copyright © 2021 Jeff Kayser
