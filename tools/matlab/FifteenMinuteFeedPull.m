function [OutputArray, NumberOfDataPoints] = FifteenMinuteFeedPull(StartYear, StartMonth, StartDay, EndYear, EndMonth, EndDay, FeedID)
i = 0;

for Month = StartMonth:1:EndMonth
    if Month == EndMonth
        LastDay = EndDay;
    elseif Month == 1 || Month == 3 || Month == 5 || Month == 7 || Month == 8 || Month == 10 || Month == 12
        LastDay = 31;
    elseif Month == 4 || Month == 6 || Month == 9 || Month == 11
        LastDay = 30;    
    elseif Month == 2
        LastDay = 28;    
    end

	% Do five days at a time at most so as not to time out.
	dayNo = 0;
    for Day = StartDay:1:LastDay
        for Hour = 0:1:23
            for Minute = 0:15:45
                params(1 + i*2) = cellstr('d');
                params(2 + i*2) = cellstr(sprintf('%04d-%02d-%02d %02d:%02d:00', StartYear, Month, Day, Hour, Minute));
                i = i+1;
            end
        end
        
        dayNo = dayNo + 1;
        
        if dayNo == 3 || Day == LastDay - 1
        	dayNo = 0;
        	i = 0;

        	
        	Feed = num2str(FeedID);
			URL = horzcat('http://smartgrid.cs.du.edu/feed/api/1/closest/', Feed, '/180/csv/?key=fd93e714-4f1a-4852-86ba-588d0e524768');
			String = urlread(URL, 'post', params)
        	
        	if exist('Output')
        		Output = horzcat(Output, char(10), String);
        	else
        		Output = String;
        	end
        end
    end
end

OutputArrayTmp = regexp(Output,'\n','split');
OutputArrayTmp = sort(OutputArrayTmp);

OutputArray = [];
for Line = 1:1:numel(OutputArrayTmp)
	OutputArray = [OutputArray; strrep(regexp(OutputArrayTmp(Line),',.+','matchstring','once'), ',','') ];
end

OutputArray = OutputArray(2:length(OutputArray)); % Remove the garbage first line.

NumberOfDataPoints = numel(OutputArray);
end
