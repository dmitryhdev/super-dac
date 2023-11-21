resource "aws_ecs_task_definition" "oktadac" {
  family                   = "hello-oktadac-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  
  container_definitions = <<DEFINITION
    [
        {
        "name"      : "oktadac",
        "image"     : "public.ecr.aws/b2m0y9g8/okta-dac:latest",
        "cpu"       : 256,
        "memory"    : 512,
        "essential" : true,
        "portMappings" : [
            {
            "containerPort" : 80,
            "hostPort"      : 80
            }
        ]
        }
    ]
    DEFINITION
}

resource "aws_ecs_cluster" "main" {
  name = "pat-cluster"
}

resource "aws_ecs_service" "hello_oktadac" {
  name            = "hello-oktadac-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.oktadac.arn
  desired_count   = var.app_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [aws_security_group.oktadac_task.id]
    subnets         = aws_subnet.private.*.id
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.hello_oktadac.id
    container_name   = "oktadac"
    container_port   = var.container_port
  }

  depends_on = [
    aws_lb_listener.hello_oktadac
  ]
}


