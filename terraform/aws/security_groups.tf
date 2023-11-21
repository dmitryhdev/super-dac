resource "aws_security_group" "lb" {
  name   = "pat-alb-security-group"
  vpc_id = aws_vpc.pat_vpc.id

  ingress {
    protocol    = "tcp"
    from_port   = 80 # HTTP
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"] # Allow all
  }

  egress {
    protocol    = "-1" # Allow all protocols
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}


resource "aws_security_group" "oktadac_task" {
  name   = "oktadac-security-group"
  vpc_id = aws_vpc.pat_vpc.id

  ingress {
    protocol        = "tcp"
    from_port       = var.container_port
    to_port         = var.container_port
    security_groups = [aws_security_group.lb.id]
    cidr_blocks     = ["0.0.0.0/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}
