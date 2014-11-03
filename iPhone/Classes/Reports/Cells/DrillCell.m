//
//  DrillCell.m
//  ConcurMobile
//
//  Created by yiwen on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "DrillCell.h"
#import "ImageUtil.h"

@implementation DrillCell
@synthesize lblName, imgIcon;

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



-(void) resetCellContent:(NSString*) name withImage:(NSString*)imgName
{
    self.lblName.text = name;
    self.imgIcon.image = [ImageUtil getImageByName:imgName];
}


+(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag
{
    DrillCell *cell = (DrillCell*)[tblView dequeueReusableCellWithIdentifier:@"DrillCell"];
	if (cell == nil)
	{
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"DrillCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[DrillCell class]])
                cell = (DrillCell *)oneObject;
	}
	
    [cell resetCellContent:command withImage:imgName];
    return cell;
}

@end
