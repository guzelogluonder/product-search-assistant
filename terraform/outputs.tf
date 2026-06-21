output "app_url" {
  value = aws_elastic_beanstalk_environment.env.cname
}
