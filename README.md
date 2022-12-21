# Scala Exercise: Interval counting

Scala exercise to group objects based on date intervals and count, [click to read the instructions](./INSTRUCTIONS.md).

## Assumptions I made for the solution

Since the example interval starts at 1, it's assumed that months are rounded up,
meaning that 10 days ago falls into the 1 month category, but exactly 1 month ago is still 1 month.

It was implied that the grouping was mutually exclusive (no overlap)

Since the grouping of an order that has products on more than one interval wasn't specified, I assumed
*"groups the orders based on the product age"* meant to group products with orders and not orders, meaning that 
if the case above occurs, one order will be counted in multiple intervals.

It's is assumed that the custom interval list starts also from current time, so it is necessary to provide 1 as first number.

## Running the solution
Have scala installed, preferably 2.13

Go to `target/scala-2.13/`

Run `scala orders_2.13-1.0.jar` with 2 dates in `yyyy-MM-dd HH:mm:ss` format and optionally a comma separated list of numbers for custom month intervals.

### Examples
```bash
$ scala orders_2.13-1.0.jar "2020-01-01 00:00:00" "2021-01-01 00:00:00"
$ scala orders_2.13-1.0.jar "2020-01-01 00:00:00" "2021-01-01 00:00:00" "1,3,7,12,30"
```

## Running tests
I made a few tests for the core functionality, you can run these with `sbt test` on the project folder.