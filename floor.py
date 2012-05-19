

def generate_floor(min_x, max_x, min_z, max_z):
	interval = 20
	x_count=min_x
	z_count=min_z

	while (x_count<max_x):
		x1 = x_count
		x2 = x_count+interval
		while (z_count<max_z):
			z1 = z_count
			z2 = z_count+interval
			print '<surface type="Box">'	
			print '<minpt>'+str(x1)+' -90 '+str(z1)+'</minpt>'
			print '<maxpt>'+str(x2)+' -89.95 '+str(z2)+'</maxpt>'
			print '<shader ref="blackfloor"/>'
			print '</surface>'
			z_count+=(interval*2)
		z_count=min_z
		x1 = x_count+interval
		x2 = x_count+(interval*2)
		while (z_count<max_z):
			z1 = z_count+interval
			z2 = z_count+(interval*2)
			print '<surface type="Box">'	
			print '<minpt>'+str(x1)+' -90 '+str(z1)+'</minpt>'
			print '<maxpt>'+str(x2)+' -89.95 '+str(z2)+'</maxpt>'
			print '<shader ref="blackfloor"/>'
			print '</surface>'
			z_count+=(interval*2)
		z_count=min_z
		x_count+=(interval*2)

generate_floor(-200,200,-200,200)