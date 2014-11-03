//
//  ApproveReportsCell.m
//  ConcurMobile
//
//  Created by Yuri on 2/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportExpenseDetailCell.h"

static int traceLevel = 2;

#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveReportExpenseDetailCell

@synthesize labelText;
@synthesize labelDetail;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
	
    return self;
}

- (NSString *) reuseIdentifier {
	return @"ApproveReportExpenseDetailCell";
}

- (void)dealloc {

	[labelText release];
	[labelDetail release];
    [super dealloc];
}


@end
