//
//  AttendeeViewDetailVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 5/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeeViewDetailVC.h"

@interface AttendeeViewDetailVC ()

@end

@implementation AttendeeViewDetailVC

@synthesize atnColumns, attendee;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)viewDidLoad
{
    [self initFields];
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.title = [@"Attendee" localize];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

-(BOOL) canEdit
{
    return NO;
}

-(void)initFields
{
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.sections = [[NSMutableArray alloc] initWithObjects:@"Data", nil];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	
    self.allFields = [[NSMutableArray alloc] init];
    
    for (FormFieldData *fld in self.atnColumns)
    {
        if (![fld.access isEqualToString:@"HD"] && ![fld.iD isEqualToString:@"InstanceCount"])
        {
            FormFieldData *field = (FormFieldData*)[fld copyWithZone:nil];
            field.access = @"RO";
            field.fieldValue = [attendee getNonNullableValueForFieldId:field.iD];
            [fields addObject:field];
            [self.allFields addObject:field];
        }
    }
    
    sectionFieldsMap[@"Data"] = fields;
    [super initFields];    
}

@end
