//
//  FormFieldCell.m
//  ConcurMobile
//
//  Created by yiwen on 4/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "FormFieldCell.h"
#import "DateTimeFormatter.h"
#import "Config.h"

@implementation FormFieldCell
@synthesize lblLabel, lblValue, lblErrMsg, lblConnListLevel, field;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


#pragma mark -
#pragma mark Cell data initilation Methods 
+(UIColor*) getLabelColor
{
    return [UIColor colorWithRed:100.0/255 green:100.0/255 blue:100.0/255 alpha:1];
}

-(void) resetCellContent:(FormFieldData*) fld
{
    self.field = fld;
    
    if ([fld isRequired] && [fld isEditable])
	{
		if ([fld isMissingValue]) 		{
			self.lblLabel.textColor = [UIColor redColor];
			self.lblLabel.highlightedTextColor = [UIColor whiteColor];
		}
		else 
		{
			self.lblLabel.textColor = [FormFieldCell getLabelColor];
			self.lblLabel.highlightedTextColor = [UIColor whiteColor];
		}
		self.lblLabel.text = [NSString stringWithFormat:@"%@ *", fld.label];
	}
	else
	{
		self.lblLabel.textColor = [FormFieldCell getLabelColor];
		self.lblLabel.highlightedTextColor = [UIColor whiteColor];
		self.lblLabel.text = fld.label;
	}
    
    // dates should have already been localized
    if ([fld.dataType isEqualToString:@"TIMESTAMP"])
    {
        if ([fld.fieldValue lengthIgnoreWhitespace])
        {
            //MOB-15485 - Get date formatter based on 12/24hr setting
            NSDate *date = nil;
            // Gov does not require TIME in the endpoint "SaveTMExpenseForm"
            // http://10.24.61.100/qawiki/index.php/TravelManager_MWS_Endpoints#SaveTMExpenseForm
            if ([Config isGov])
            {
                date = [DateTimeFormatter getNSDateForGov:fld.fieldValue Format:[CCDateUtilities getDateFormatString] TimeZone:[NSTimeZone localTimeZone]];
            }
            else
            {
                date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:fld.fieldValue];
            }
            
            if (date != nil)
                self.lblValue.text = [CCDateUtilities formatDateToEEEMonthDayYear:date];
            else
                // MOB-9945 Prevent "(null) (null)" from corrupting the date field
                self.lblValue.text = @"";
        }
        else
        {
            self.lblValue.text = @"";
        }
    }
    else
    {
        if ([fld needsSecureEntry] && [fld.fieldValue length])
            self.lblValue.text = @"********";
        else
        {
            
            // MOB-11039
            BOOL showCode = [field.dataType isEqualToString:@"MLIST"];
            showCode &= [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"MobileViewPicklistCodes" withType:@"OTMODULE"]];
            
            if (showCode && fld.liKey != nil)
            {
                self.lblValue.text = [NSString stringWithFormat:@"(%@) %@", fld.liCode, fld.fieldValue];
            }
            else
            {
                self.lblValue.text = fld.fieldValue;
            }
            
            
        }
    }
	if ([fld isEditable] || [fld.iD isEqualToString:@"Comment"])
		self.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	else
		self.accessoryType = UITableViewCellAccessoryNone;
    
    CGFloat x = 10;
    if ([fld.dataType isEqualToString:@"MLIST"] && [fld isEditable])
    {
        x = 28;
        self.lblConnListLevel.text = [NSString stringWithFormat:@"%d", fld.hierLevel];
        [self.lblConnListLevel setHidden:NO];
    }
    else
        [self.lblConnListLevel setHidden:YES];
    
    // Wait on UI guidelines to resize for validation errors
	if (fld.validationErrMsg != nil)
	{
		[self.lblErrMsg setHidden:NO];
		self.lblErrMsg.text = fld.validationErrMsg;
		CGRect rect = self.lblValue.frame;
		self.lblValue.frame = CGRectMake(rect.origin.x, 25, rect.size.width, 16);
        self.lblValue.font = [UIFont fontWithName:@"HelveticaNeue" size:14.0f];
        
        rect = self.lblLabel.frame;
        self.lblLabel.frame = CGRectMake(x, 9, rect.size.width, rect.size.height);
        self.lblConnListLevel.frame = CGRectMake(10, 9, 12, rect.size.height);
	}
	else {
        [self.lblErrMsg setHidden:YES];
        if (![fld.fieldValue length])
        {
            CGRect rect = self.lblLabel.frame;
            self.lblLabel.frame = CGRectMake(x, 22, rect.size.width, rect.size.height);
            self.lblConnListLevel.frame = CGRectMake(10, 22, 12, rect.size.height);
        }
		else
        {
            CGRect rect = self.lblValue.frame;
            self.lblValue.frame = CGRectMake(rect.origin.x, 32, rect.size.width, 18);
            self.lblValue.font = [UIFont fontWithName:@"HelveticaNeue" size:16.0f];
            rect = self.lblLabel.frame;
            self.lblLabel.frame = CGRectMake(x, 10, rect.size.width, rect.size.height);
            self.lblConnListLevel.frame = CGRectMake(10, 10, 12, rect.size.height);
        }
    }    
}

@end
