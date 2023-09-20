os.loadAPI("json.lua")

shipreader = peripheral.find("ship_reader")
monitor = peripheral.find("monitor")
default_mass = shipreader.getMass()

function newLine()
	x,y = monitor.getCursorPos()
	monitor.setCursorPos(1,y+1)
end

function initMassDictionaryJSON()
	obj = json.decode(textutils.serializeJSON( {} ))
	return obj
end

function getMassDictionaryJSON()
	local h = fs.open("mass_dictionary.txt","r")
	serialized_dict = h.readAll()
	obj = json.decode(serialized_dict)
	h.close()
	return obj
end

function saveMassDictionary(md_obj)
	local h = fs.open("mass_dictionary.txt","w")
	h.writeLine(json.encodePretty(md_obj))
	h.flush()
	h.close()
end

function addToMassDictionary(md_obj,entry)
	md_obj[table.getn(md_obj)+1] = entry
end

function isUniqueEntry(md_obj,entry_id)
	for k, v in pairs(md_obj) do
	  if v.id == entry_id then
		return false
	  end
	end
	return true
end

--mass_dictionary = initMassDictionaryJSON()
mass_dictionary = getMassDictionaryJSON()

save = false
while true do
	term.clear()
	term.setCursorPos(1,1)
	monitor.clear()
	monitor.setCursorPos(1,1)
	
	local success,data = turtle.inspect()
	id = ""
	mass = ""
	if(success) then
		id = data.name
		mass = shipreader.getMass()-default_mass
		print("Block ID: "..id)
		print("Mass: "..mass)
		
		monitor.write("Block ID: ")
		newLine()
		monitor.write(data.name)
		newLine()
		monitor.write("Mass:")
		newLine()
		monitor.write(shipreader.getMass()-default_mass)
		
		if save then
			new_entry = {id=data.name,mass=shipreader.getMass()-default_mass}
			if isUniqueEntry(mass_dictionary,new_entry.id) then
				addToMassDictionary(mass_dictionary,new_entry)
			end
			saveMassDictionary(mass_dictionary)
			save = false
		end
		
	else
		save = true
		print("Please Place Block ON turtle...")
		monitor.write("Please Place Block ON turtle...")
		newLine()
		monitor.write("Mass:")
		newLine()
		monitor.write(shipreader.getMass()-default_mass)
	end
	
	
	sleep(0.05)

end
