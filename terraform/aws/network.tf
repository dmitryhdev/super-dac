resource "aws_vpc" "pat_vpc" {
  cidr_block = var.cidr_block
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.pat_vpc.id
  count                   = length(var.public_subnet_cidr_block)
  cidr_block              = element(var.public_subnet_cidr_block, count.index)
  availability_zone       = element(var.availability_zones, count.index)
  map_public_ip_on_launch = true
}

resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.pat_vpc.id
  count             = length(var.private_subnet_cidr_block)
  cidr_block        = element(var.private_subnet_cidr_block, count.index)
  availability_zone = element(var.availability_zones, count.index)
}

resource "aws_internet_gateway" "gateway" {
  vpc_id = aws_vpc.pat_vpc.id
}

# Public
resource "aws_route" "public" {
  route_table_id         = aws_vpc.pat_vpc.main_route_table_id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.gateway.id
}

# Private
resource "aws_nat_gateway" "gateway" {
  count         = length(var.public_subnet_cidr_block)
  subnet_id     = element(aws_subnet.public.*.id, count.index)
  allocation_id = element(aws_eip.gateway.*.id, count.index)
  depends_on = [
    aws_internet_gateway.gateway
  ]
}

resource "aws_eip" "gateway" {
  vpc        = true
  count      = length(var.public_subnet_cidr_block)
  depends_on = [aws_internet_gateway.gateway]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.pat_vpc.id
  count  = length(var.private_subnet_cidr_block)

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = element(aws_nat_gateway.gateway.*.id, count.index)
  }
}

resource "aws_route_table_association" "private" {
  count          = length(var.private_subnet_cidr_block)
  subnet_id      = element(aws_subnet.private.*.id, count.index)
  route_table_id = element(aws_route_table.private.*.id, count.index)
}
