//
//  ReportApprovalListCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportApprovalListCell.h"


@implementation ReportApprovalListCell

@synthesize lblName, lblAmount, lblLine1, lblLine2, rpt;
@synthesize img1, img2, img3, img4, img5, img6;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


-(void) clearAllImagesInCell
{
	img1.image = nil;
	img2.image = nil;
	img3.image = nil;
	img4.image = nil;
	img5.image = nil;
	img6.image = nil;
}

-(void) setImageByPosition:(int)imagePos imageName:(NSString *)imgName
{
	UIImage *img = imgName == nil? nil : [UIImage imageNamed:imgName];
	switch (imagePos)
	{
		case 0:
			img1.image = nil;
			img1.image = img;
			break;
		case 1:
			img2.image = nil;
			img2.image = img;
			break;
		case 2:
			img3.image = nil;
			img3.image = img;
			break;
		case 3:
			img4.image = nil;
			img4.image = img;
			break;
		case 4:
			img5.image = nil;
			img5.image = img;
			break;
		case 5:
			img6.image = nil;
			img6.image = img;
			break;
			
		default:
			
			break;
	}
	//img = nil;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    float indentPoints = self.indentationLevel * self.indentationWidth;
    
    self.contentView.frame = CGRectMake(
                                        indentPoints,
                                        self.contentView.frame.origin.y,
                                        self.contentView.frame.size.width - indentPoints,
                                        self.contentView.frame.size.height
                                        );
}


@end
